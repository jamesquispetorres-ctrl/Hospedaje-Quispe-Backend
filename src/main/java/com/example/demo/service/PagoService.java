package com.example.demo.service;

import com.example.demo.dto.PagoDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.*;
import com.example.demo.model.enums.EstadoPago;
import com.example.demo.model.enums.MetodoPago;
import com.example.demo.repository.ContratoRepository;
import com.example.demo.repository.PagoRepository;
import com.example.demo.repository.ServicioAdicionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ContratoRepository contratoRepository;
    private final ServicioAdicionalRepository servicioRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<PagoDTO.Response> getAllPagos() {
        return pagoRepository.findAllWithDetails().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagoDTO.Response getPagoById(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + id));
        return toResponseDTO(pago);
    }

    @Transactional(readOnly = true)
    public List<PagoDTO.Response> getPagosByContrato(Long contratoId) {
        return pagoRepository.findByContratoIdOrderByFechaVencimientoDesc(contratoId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PagoDTO.Response createPago(PagoDTO.Request request, MultipartFile comprobante) {
        Contrato contrato = contratoRepository.findById(request.getContratoId())
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado con ID: " + request.getContratoId()));

        BigDecimal montoHabitacion = request.getMontoHabitacion() != null 
                ? request.getMontoHabitacion() 
                : contrato.getMontoMensualPactado();

        // Procesar servicios adicionales de tarifa fija
        List<PagoServicio> listaServicios = new ArrayList<>();
        BigDecimal montoServicios = BigDecimal.ZERO;

        if (request.getServiciosIds() != null && !request.getServiciosIds().isEmpty()) {
            for (Long servId : request.getServiciosIds()) {
                ServicioAdicional serv = servicioRepository.findById(servId)
                        .orElseThrow(() -> new ResourceNotFoundException("Servicio adicional no encontrado con ID: " + servId));
                
                PagoServicio detalle = PagoServicio.builder()
                        .servicioAdicional(serv)
                        .nombreServicio(serv.getNombre())
                        .costoCobrado(serv.getTarifaFija())
                        .build();

                listaServicios.add(detalle);
                montoServicios = montoServicios.add(serv.getTarifaFija());
            }
        }

        BigDecimal montoTotal = montoHabitacion.add(montoServicios);

        // Validar comprobante para Transferencia o Yape si se registra como pagado
        String comprobanteUrl = null;
        if (comprobante != null && !comprobante.isEmpty()) {
            comprobanteUrl = fileStorageService.storeFile(comprobante);
        }

        EstadoPago estado = request.getEstadoPago();
        if (estado == null) {
            estado = (request.getMetodoPago() != null || request.getFechaPago() != null) 
                    ? EstadoPago.PAGADO 
                    : EstadoPago.PENDIENTE;
        }

        LocalDate fechaPago = request.getFechaPago();
        if (estado == EstadoPago.PAGADO && fechaPago == null) {
            fechaPago = LocalDate.now();
        }

        BigDecimal montoPagado = BigDecimal.ZERO;
        BigDecimal saldoPendiente = montoTotal;

        if (estado == EstadoPago.PAGADO) {
            montoPagado = montoTotal;
            saldoPendiente = BigDecimal.ZERO;
        } else if (request.getMontoAbonado() != null && request.getMontoAbonado().compareTo(BigDecimal.ZERO) > 0) {
            if (request.getMontoAbonado().compareTo(montoTotal) >= 0) {
                montoPagado = montoTotal;
                saldoPendiente = BigDecimal.ZERO;
                estado = EstadoPago.PAGADO;
            } else {
                montoPagado = request.getMontoAbonado();
                saldoPendiente = montoTotal.subtract(montoPagado);
                estado = EstadoPago.PARCIAL;
            }
        }

        Pago pago = Pago.builder()
                .contrato(contrato)
                .periodoMesAnio(request.getPeriodoMesAnio().trim())
                .fechaVencimiento(request.getFechaVencimiento())
                .fechaPago(fechaPago)
                .montoHabitacion(montoHabitacion)
                .montoServicios(montoServicios)
                .montoTotal(montoTotal)
                .montoPagado(montoPagado)
                .saldoPendiente(saldoPendiente)
                .metodoPago(request.getMetodoPago())
                .estadoPago(estado)
                .comprobanteUrl(comprobanteUrl)
                .observaciones(request.getObservaciones())
                .build();

        for (PagoServicio ps : listaServicios) {
            pago.addServicio(ps);
        }

        Pago guardado = pagoRepository.save(pago);
        return toResponseDTO(guardado);
    }

    @Transactional
    public PagoDTO.Response registrarPagoEfectuado(Long pagoId, MetodoPago metodo, LocalDate fechaPago, 
                                                  BigDecimal montoAbonado, List<Long> nuevosServiciosIds, 
                                                  String observaciones, MultipartFile comprobante) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + pagoId));

        if (pago.getEstadoPago() == EstadoPago.PAGADO) {
            throw new BadRequestException("El pago con ID " + pagoId + " ya fue registrado como PAGADO.");
        }

        // Subir comprobante si se adjunta
        if (comprobante != null && !comprobante.isEmpty()) {
            pago.setComprobanteUrl(fileStorageService.storeFile(comprobante));
        }

        // Actualizar servicios si se envían
        if (nuevosServiciosIds != null) {
            pago.getServicios().clear();
            BigDecimal nuevoMontoServicios = BigDecimal.ZERO;
            for (Long servId : nuevosServiciosIds) {
                ServicioAdicional serv = servicioRepository.findById(servId)
                        .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + servId));
                PagoServicio detalle = PagoServicio.builder()
                        .servicioAdicional(serv)
                        .nombreServicio(serv.getNombre())
                        .costoCobrado(serv.getTarifaFija())
                        .build();
                pago.addServicio(detalle);
                nuevoMontoServicios = nuevoMontoServicios.add(serv.getTarifaFija());
            }
            pago.setMontoServicios(nuevoMontoServicios);
            pago.setMontoTotal(pago.getMontoHabitacion().add(nuevoMontoServicios));
        }

        BigDecimal montoPagadoActual = pago.getMontoPagado() != null ? pago.getMontoPagado() : BigDecimal.ZERO;
        BigDecimal saldoActual = pago.getMontoTotal().subtract(montoPagadoActual);

        // Lógica de abono parcial o pago completo
        if (montoAbonado == null || montoAbonado.compareTo(BigDecimal.ZERO) <= 0 || montoAbonado.compareTo(saldoActual) >= 0) {
            // Se cancela la totalidad del saldo restante
            pago.setMontoPagado(pago.getMontoTotal());
            pago.setSaldoPendiente(BigDecimal.ZERO);
            pago.setEstadoPago(EstadoPago.PAGADO);
        } else {
            // Pago en partes (abono parcial)
            BigDecimal nuevoMontoPagado = montoPagadoActual.add(montoAbonado);
            BigDecimal nuevoSaldo = pago.getMontoTotal().subtract(nuevoMontoPagado);
            pago.setMontoPagado(nuevoMontoPagado);
            pago.setSaldoPendiente(nuevoSaldo);
            pago.setEstadoPago(nuevoSaldo.compareTo(BigDecimal.ZERO) == 0 ? EstadoPago.PAGADO : EstadoPago.PARCIAL);
        }

        pago.setMetodoPago(metodo != null ? metodo : MetodoPago.EFECTIVO);
        pago.setFechaPago(fechaPago != null ? fechaPago : LocalDate.now());
        if (observaciones != null && !observaciones.isBlank()) {
            String anterior = (pago.getObservaciones() != null && !pago.getObservaciones().isBlank()) 
                    ? pago.getObservaciones() + " | " 
                    : "";
            pago.setObservaciones(anterior + observaciones.trim());
        }

        Pago actualizado = pagoRepository.save(pago);
        return toResponseDTO(actualizado);
    }

    @Transactional
    public List<PagoDTO.Response> registrarPagoAdelantadoMultiMes(PagoDTO.AdelantadoRequest req, MultipartFile comprobante) {
        Contrato contrato = contratoRepository.findById(req.getContratoId())
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado con ID: " + req.getContratoId()));

        if (req.getCantidadMeses() == null || req.getCantidadMeses() < 1) {
            throw new BadRequestException("La cantidad de meses a adelantar debe ser de al menos 1.");
        }

        String comprobanteUrl = null;
        if (comprobante != null && !comprobante.isEmpty()) {
            comprobanteUrl = fileStorageService.storeFile(comprobante);
        }

        // Obtener servicios adicionales
        List<ServicioAdicional> serviciosAdicionales = new ArrayList<>();
        BigDecimal montoServiciosTotal = BigDecimal.ZERO;
        if (req.getServiciosIds() != null && !req.getServiciosIds().isEmpty()) {
            for (Long sid : req.getServiciosIds()) {
                ServicioAdicional s = servicioRepository.findById(sid)
                        .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + sid));
                serviciosAdicionales.add(s);
                montoServiciosTotal = montoServiciosTotal.add(s.getTarifaFija());
            }
        }

        java.time.YearMonth mesInicio;
        try {
            mesInicio = java.time.YearMonth.parse(req.getMesInicio().trim());
        } catch (Exception e) {
            mesInicio = java.time.YearMonth.now();
        }

        List<Pago> pagosCreados = new ArrayList<>();
        int diaPago = contrato.getDiaPagoMensual();

        for (int i = 0; i < req.getCantidadMeses(); i++) {
            java.time.YearMonth mesPeriodo = mesInicio.plusMonths(i);
            String periodoStr = mesPeriodo.toString(); // "YYYY-MM"
            LocalDate fechaVencimiento = mesPeriodo.atDay(Math.min(diaPago, mesPeriodo.lengthOfMonth()));
            BigDecimal montoHab = contrato.getMontoMensualPactado();
            BigDecimal totalMes = montoHab.add(montoServiciosTotal);

            // Verificar si ya existe un pago para ese contrato y periodo
            Pago pago = pagoRepository.findByContratoIdAndPeriodoMesAnio(contrato.getId(), periodoStr)
                    .orElse(null);

            if (pago == null) {
                pago = Pago.builder()
                        .contrato(contrato)
                        .periodoMesAnio(periodoStr)
                        .fechaVencimiento(fechaVencimiento)
                        .montoHabitacion(montoHab)
                        .montoServicios(montoServiciosTotal)
                        .montoTotal(totalMes)
                        .build();

                for (ServicioAdicional s : serviciosAdicionales) {
                    PagoServicio ps = PagoServicio.builder()
                            .servicioAdicional(s)
                            .nombreServicio(s.getNombre())
                            .costoCobrado(s.getTarifaFija())
                            .build();
                    pago.addServicio(ps);
                }
            }

            pago.setMontoPagado(totalMes);
            pago.setSaldoPendiente(BigDecimal.ZERO);
            pago.setEstadoPago(EstadoPago.PAGADO);
            pago.setMetodoPago(req.getMetodoPago() != null ? req.getMetodoPago() : MetodoPago.EFECTIVO);
            pago.setFechaPago(req.getFechaPago() != null ? req.getFechaPago() : LocalDate.now());
            if (comprobanteUrl != null) {
                pago.setComprobanteUrl(comprobanteUrl);
            }
            String nota = "Pago adelantado (Mes " + (i + 1) + " de " + req.getCantidadMeses() + ")";
            if (req.getObservaciones() != null && !req.getObservaciones().isBlank()) {
                nota += " - " + req.getObservaciones().trim();
            }
            pago.setObservaciones(nota);

            pagosCreados.add(pagoRepository.save(pago));
        }

        return pagosCreados.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    public PagoDTO.Response toResponseDTO(Pago entity) {
        List<PagoDTO.ServicioCobradoDTO> serviciosDTO = entity.getServicios().stream()
                .map(s -> PagoDTO.ServicioCobradoDTO.builder()
                        .id(s.getId())
                        .servicioAdicionalId(s.getServicioAdicional() != null ? s.getServicioAdicional().getId() : null)
                        .nombreServicio(s.getNombreServicio())
                        .costoCobrado(s.getCostoCobrado())
                        .build())
                .collect(Collectors.toList());

        Huesped h = entity.getContrato().getHuesped();
        Habitacion hab = entity.getContrato().getHabitacion();

        BigDecimal montoPagado = entity.getMontoPagado() != null ? entity.getMontoPagado() : BigDecimal.ZERO;
        BigDecimal saldoPendiente = entity.getSaldoPendiente();

        if (entity.getEstadoPago() == EstadoPago.PAGADO) {
            montoPagado = entity.getMontoTotal();
            saldoPendiente = BigDecimal.ZERO;
        } else if (saldoPendiente == null || (saldoPendiente.compareTo(BigDecimal.ZERO) == 0 && montoPagado.compareTo(BigDecimal.ZERO) == 0)) {
            saldoPendiente = entity.getMontoTotal();
        }

        return PagoDTO.Response.builder()
                .id(entity.getId())
                .contratoId(entity.getContrato().getId())
                .huespedId(h.getId())
                .huespedNombreCompleto(h.getNombres() + " " + h.getApellidos())
                .huespedDni(h.getDni())
                .huespedTelefono(h.getTelefono())
                .habitacionNumero(hab.getNumero())
                .habitacionPiso(hab.getPiso())
                .periodoMesAnio(entity.getPeriodoMesAnio())
                .fechaVencimiento(entity.getFechaVencimiento())
                .fechaPago(entity.getFechaPago())
                .montoHabitacion(entity.getMontoHabitacion())
                .montoServicios(entity.getMontoServicios())
                .montoTotal(entity.getMontoTotal())
                .montoPagado(montoPagado)
                .saldoPendiente(saldoPendiente)
                .metodoPago(entity.getMetodoPago())
                .estadoPago(entity.getEstadoPago())
                .comprobanteUrl(entity.getComprobanteUrl())
                .observaciones(entity.getObservaciones())
                .servicios(serviciosDTO)
                .fechaRegistro(entity.getFechaRegistro())
                .build();
    }
}
