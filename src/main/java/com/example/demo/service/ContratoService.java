package com.example.demo.service;

import com.example.demo.dto.ContratoDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Contrato;
import com.example.demo.model.entity.Habitacion;
import com.example.demo.model.entity.Huesped;
import com.example.demo.model.entity.Pago;
import com.example.demo.model.enums.EstadoContrato;
import com.example.demo.model.enums.EstadoHabitacion;
import com.example.demo.model.enums.EstadoPago;
import com.example.demo.repository.ContratoRepository;
import com.example.demo.repository.HabitacionRepository;
import com.example.demo.repository.HuespedRepository;
import com.example.demo.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final HuespedRepository huespedRepository;
    private final HabitacionRepository habitacionRepository;
    private final PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    public List<ContratoDTO.Response> getAllContratos() {
        return contratoRepository.findAllOrderByFechaCreacionDesc().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContratoDTO.Response> getContratosActivos() {
        return contratoRepository.findByEstado(EstadoContrato.ACTIVO).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContratoDTO.Response getContratoById(Long id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado con ID: " + id));
        return toResponseDTO(contrato);
    }

    @Transactional
    public ContratoDTO.Response createContrato(ContratoDTO.Request request) {
        Huesped huesped = huespedRepository.findById(request.getHuespedId())
                .orElseThrow(() -> new ResourceNotFoundException("Huésped no encontrado con ID: " + request.getHuespedId()));

        if (!Boolean.TRUE.equals(huesped.getActivo())) {
            throw new BadRequestException("El huésped no se encuentra en estado activo.");
        }

        Habitacion habitacion = habitacionRepository.findById(request.getHabitacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada con ID: " + request.getHabitacionId()));

        if (habitacion.getEstado() != EstadoHabitacion.DISPONIBLE) {
            throw new BadRequestException("La habitación " + habitacion.getNumero() + " no está disponible. Estado actual: " + habitacion.getEstado());
        }

        BigDecimal montoPactado = request.getMontoMensualPactado() != null 
                ? request.getMontoMensualPactado() 
                : habitacion.getPrecioMensual();

        Contrato contrato = Contrato.builder()
                .huesped(huesped)
                .habitacion(habitacion)
                .fechaInicio(request.getFechaInicio())
                .diaPagoMensual(request.getDiaPagoMensual())
                .montoMensualPactado(montoPactado)
                .estado(EstadoContrato.ACTIVO)
                .observaciones(request.getObservaciones())
                .build();

        // Marcar la habitación como OCUPADA
        habitacion.setEstado(EstadoHabitacion.OCUPADA);
        habitacionRepository.save(habitacion);

        Contrato guardado = contratoRepository.save(contrato);

        // Generar automáticamente el primer pago mensual pendiente según la fecha de inicio
        LocalDate fechaInicio = request.getFechaInicio();
        int diaPago = request.getDiaPagoMensual();
        LocalDate vencimientoPrimerMes;
        try {
            vencimientoPrimerMes = LocalDate.of(fechaInicio.getYear(), fechaInicio.getMonth(), Math.min(diaPago, fechaInicio.lengthOfMonth()));
            if (vencimientoPrimerMes.isBefore(fechaInicio)) {
                vencimientoPrimerMes = vencimientoPrimerMes.plusMonths(1);
            }
        } catch (Exception e) {
            vencimientoPrimerMes = fechaInicio.plusMonths(1);
        }

        Pago primerPago = Pago.builder()
                .contrato(guardado)
                .periodoMesAnio(vencimientoPrimerMes.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .fechaVencimiento(vencimientoPrimerMes)
                .montoHabitacion(montoPactado)
                .montoServicios(BigDecimal.ZERO)
                .montoTotal(montoPactado)
                .estadoPago(EstadoPago.PENDIENTE)
                .observaciones("Pago inicial generado automáticamente al aperturar contrato.")
                .build();

        pagoRepository.save(primerPago);

        return toResponseDTO(guardado);
    }

    @Transactional
    public ContratoDTO.Response finalizarContrato(Long id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado con ID: " + id));

        if (contrato.getEstado() == EstadoContrato.FINALIZADO) {
            throw new BadRequestException("El contrato ya se encuentra finalizado.");
        }

        contrato.setEstado(EstadoContrato.FINALIZADO);
        contrato.setFechaFin(LocalDate.now());

        // Liberar la habitación para que vuelva a estar DISPONIBLE
        Habitacion habitacion = contrato.getHabitacion();
        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
        habitacionRepository.save(habitacion);

        Contrato actualizado = contratoRepository.save(contrato);
        return toResponseDTO(actualizado);
    }

    public ContratoDTO.Response toResponseDTO(Contrato entity) {
        return ContratoDTO.Response.builder()
                .id(entity.getId())
                .huespedId(entity.getHuesped().getId())
                .huespedNombreCompleto(entity.getHuesped().getNombres() + " " + entity.getHuesped().getApellidos())
                .huespedDni(entity.getHuesped().getDni())
                .huespedTelefono(entity.getHuesped().getTelefono())
                .habitacionId(entity.getHabitacion().getId())
                .habitacionNumero(entity.getHabitacion().getNumero())
                .habitacionPiso(entity.getHabitacion().getPiso())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .diaPagoMensual(entity.getDiaPagoMensual())
                .montoMensualPactado(entity.getMontoMensualPactado())
                .estado(entity.getEstado())
                .observaciones(entity.getObservaciones())
                .fechaCreacion(entity.getFechaCreacion())
                .build();
    }
}
