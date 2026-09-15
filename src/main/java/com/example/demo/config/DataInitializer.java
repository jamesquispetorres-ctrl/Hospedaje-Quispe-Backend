package com.example.demo.config;

import com.example.demo.model.entity.*;
import com.example.demo.model.enums.EstadoContrato;
import com.example.demo.model.enums.EstadoHabitacion;
import com.example.demo.model.enums.EstadoPago;
import com.example.demo.model.enums.MetodoPago;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final HuespedRepository huespedRepository;
    private final HabitacionRepository habitacionRepository;
    private final ContratoRepository contratoRepository;
    private final ServicioAdicionalRepository servicioRepository;
    private final PagoRepository pagoRepository;

    @Override
    public void run(String... args) {
        // Sincronizar saldos de pagos existentes
        try {
            List<Pago> todosPagos = pagoRepository.findAll();
            for (Pago p : todosPagos) {
                boolean cambiado = false;
                if (p.getEstadoPago() == EstadoPago.PAGADO) {
                    if (p.getMontoPagado() == null || p.getMontoPagado().compareTo(p.getMontoTotal()) != 0) {
                        p.setMontoPagado(p.getMontoTotal());
                        p.setSaldoPendiente(BigDecimal.ZERO);
                        cambiado = true;
                    }
                } else if (p.getEstadoPago() == EstadoPago.PENDIENTE || p.getEstadoPago() == EstadoPago.ATRASADO) {
                    if (p.getMontoPagado() == null || p.getMontoPagado().compareTo(BigDecimal.ZERO) == 0) {
                        p.setMontoPagado(BigDecimal.ZERO);
                        p.setSaldoPendiente(p.getMontoTotal());
                        cambiado = true;
                    }
                }
                if (cambiado) {
                    pagoRepository.save(p);
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo sincronizar pagos anteriores: {}", e.getMessage());
        }

        if (habitacionRepository.count() > 0) {
            log.info("Los datos iniciales ya se encuentran cargados en la base de datos.");
            return;
        }

        log.info("Inicializando datos de prueba para Hospedaje Quispe (Zona Minera)...");

        // 1. Habitaciones por piso con precios directamente editables
        Habitacion hab101 = habitacionRepository.save(Habitacion.builder()
                .numero("101")
                .piso(1)
                .precioMensual(new BigDecimal("450.00"))
                .estado(EstadoHabitacion.OCUPADA)
                .descripcion("Piso 1, baño privado, cama de plaza y media, vista interior tranquila")
                .build());

        Habitacion hab102 = habitacionRepository.save(Habitacion.builder()
                .numero("102")
                .piso(1)
                .precioMensual(new BigDecimal("550.00"))
                .estado(EstadoHabitacion.OCUPADA)
                .descripcion("Piso 1, baño privado, cama matrimonial, closet empotrado")
                .build());

        Habitacion hab201 = habitacionRepository.save(Habitacion.builder()
                .numero("201")
                .piso(2)
                .precioMensual(new BigDecimal("480.00"))
                .estado(EstadoHabitacion.DISPONIBLE)
                .descripcion("Piso 2, baño privado, excelente iluminación natural y ventilación")
                .build());

        Habitacion hab202 = habitacionRepository.save(Habitacion.builder()
                .numero("202")
                .piso(2)
                .precioMensual(new BigDecimal("650.00"))
                .estado(EstadoHabitacion.OCUPADA)
                .descripcion("Piso 2, amplia habitación, escritorio de trabajo, baño privado con agua caliente")
                .build());

        Habitacion hab301 = habitacionRepository.save(Habitacion.builder()
                .numero("301")
                .piso(3)
                .precioMensual(new BigDecimal("400.00"))
                .estado(EstadoHabitacion.MANTENIMIENTO)
                .descripcion("Piso 3, en mantenimiento de pintura y grifería")
                .build());

        // 2. Servicios Adicionales de Tarifa Fija
        ServicioAdicional servInternet = servicioRepository.save(ServicioAdicional.builder()
                .nombre("Internet Fibra Óptica Alta Velocidad")
                .tarifaFija(new BigDecimal("40.00"))
                .activo(true)
                .descripcion("Conexión dedicada estable para streaming y teletrabajo")
                .build());

        ServicioAdicional servLuz = servicioRepository.save(ServicioAdicional.builder()
                .nombre("Consumo Eléctrico Fijo (Ducha y Termo)")
                .tarifaFija(new BigDecimal("35.00"))
                .activo(true)
                .descripcion("Tarifa fija mensual por uso continuo de calentador y artefactos")
                .build());

        ServicioAdicional servLavanderia = servicioRepository.save(ServicioAdicional.builder()
                .nombre("Servicio de Lavandería Semanal")
                .tarifaFija(new BigDecimal("50.00"))
                .activo(true)
                .descripcion("Lavado y secado semanal de prendas de trabajo de faena")
                .build());

        // 3. Huéspedes (Datos personales esenciales)
        Huesped h1 = huespedRepository.save(Huesped.builder()
                .dni("45892134")
                .nombres("Carlos Alberto")
                .apellidos("Mendoza Huamán")
                .telefono("984123456")
                .email("carlos.mendoza@gmail.com")
                .activo(true)
                .build());

        Huesped h2 = huespedRepository.save(Huesped.builder()
                .dni("71239845")
                .nombres("Javier Enrique")
                .apellidos("Rojas Quispe")
                .telefono("951987654")
                .email("javier.rojas@hotmail.com")
                .activo(true)
                .build());

        Huesped h3 = huespedRepository.save(Huesped.builder()
                .dni("40125678")
                .nombres("Wilfredo")
                .apellidos("Mamani Condori")
                .telefono("964552211")
                .email("wilfredo.mamani@gmail.com")
                .activo(true)
                .build());

        LocalDate hoy = LocalDate.now();

        // 4. Contratos de Alquiler Mensual Indefinido
        // Contrato 1: Con pago atrasado para probar alerta de mora y WhatsApp de cobro urgente
        Contrato c1 = contratoRepository.save(Contrato.builder()
                .huesped(h1)
                .habitacion(hab101)
                .fechaInicio(hoy.minusMonths(2))
                .diaPagoMensual(5)
                .montoMensualPactado(hab101.getPrecioMensual())
                .estado(EstadoContrato.ACTIVO)
                .observaciones("Contrato indefinido. Turnos de 14x7.")
                .build());

        // Contrato 2: Con próximo vencimiento en 2 días para probar alerta preventiva
        Contrato c2 = contratoRepository.save(Contrato.builder()
                .huesped(h2)
                .habitacion(hab102)
                .fechaInicio(hoy.minusMonths(1))
                .diaPagoMensual(hoy.plusDays(2).getDayOfMonth())
                .montoMensualPactado(hab102.getPrecioMensual())
                .estado(EstadoContrato.ACTIVO)
                .observaciones("Habitación ocupada por ingeniero residente.")
                .build());

        // Contrato 3: Al día
        Contrato c3 = contratoRepository.save(Contrato.builder()
                .huesped(h3)
                .habitacion(hab202)
                .fechaInicio(hoy.minusDays(20))
                .diaPagoMensual(28)
                .montoMensualPactado(hab202.getPrecioMensual())
                .estado(EstadoContrato.ACTIVO)
                .observaciones("Contrato indefinido de estancia.")
                .build());

        // 5. Pagos Mensuales con Servicios y Alertas
        // Pago Atrasado (hace 8 días)
        LocalDate vencimientoAtrasado = hoy.minusDays(8);
        Pago pagoAtrasado = Pago.builder()
                .contrato(c1)
                .periodoMesAnio(vencimientoAtrasado.getYear() + "-" + String.format("%02d", vencimientoAtrasado.getMonthValue()))
                .fechaVencimiento(vencimientoAtrasado)
                .montoHabitacion(c1.getMontoMensualPactado())
                .montoServicios(servInternet.getTarifaFija().add(servLuz.getTarifaFija()))
                .montoTotal(c1.getMontoMensualPactado().add(servInternet.getTarifaFija()).add(servLuz.getTarifaFija()))
                .estadoPago(EstadoPago.ATRASADO)
                .observaciones("Pendiente de pago. Se llamó al huésped sin respuesta.")
                .build();
        pagoAtrasado.addServicio(PagoServicio.builder()
                .servicioAdicional(servInternet)
                .nombreServicio(servInternet.getNombre())
                .costoCobrado(servInternet.getTarifaFija())
                .build());
        pagoAtrasado.addServicio(PagoServicio.builder()
                .servicioAdicional(servLuz)
                .nombreServicio(servLuz.getNombre())
                .costoCobrado(servLuz.getTarifaFija())
                .build());
        pagoRepository.save(pagoAtrasado);

        // Pago Próximo a Vencer (en 2 días)
        LocalDate vencimientoProximo = hoy.plusDays(2);
        Pago pagoProximo = Pago.builder()
                .contrato(c2)
                .periodoMesAnio(vencimientoProximo.getYear() + "-" + String.format("%02d", vencimientoProximo.getMonthValue()))
                .fechaVencimiento(vencimientoProximo)
                .montoHabitacion(c2.getMontoMensualPactado())
                .montoServicios(servInternet.getTarifaFija())
                .montoTotal(c2.getMontoMensualPactado().add(servInternet.getTarifaFija()))
                .estadoPago(EstadoPago.PENDIENTE)
                .observaciones("Cuota mensual próxima a vencer.")
                .build();
        pagoProximo.addServicio(PagoServicio.builder()
                .servicioAdicional(servInternet)
                .nombreServicio(servInternet.getNombre())
                .costoCobrado(servInternet.getTarifaFija())
                .build());
        pagoRepository.save(pagoProximo);

        // Pago ya cancelado con YAPE y servicios adicionales
        LocalDate vencimientoPagado = hoy.minusMonths(1);
        Pago pagoCancelado = Pago.builder()
                .contrato(c3)
                .periodoMesAnio(vencimientoPagado.getYear() + "-" + String.format("%02d", vencimientoPagado.getMonthValue()))
                .fechaVencimiento(vencimientoPagado)
                .fechaPago(vencimientoPagado.minusDays(1))
                .montoHabitacion(c3.getMontoMensualPactado())
                .montoServicios(servLuz.getTarifaFija())
                .montoTotal(c3.getMontoMensualPactado().add(servLuz.getTarifaFija()))
                .metodoPago(MetodoPago.YAPE)
                .estadoPago(EstadoPago.PAGADO)
                .observaciones("Cancelado a tiempo por Yape.")
                .build();
        pagoCancelado.addServicio(PagoServicio.builder()
                .servicioAdicional(servLuz)
                .nombreServicio(servLuz.getNombre())
                .costoCobrado(servLuz.getTarifaFija())
                .build());
        pagoRepository.save(pagoCancelado);

        log.info("¡Datos iniciales cargados con éxito! Se cargaron habitaciones, servicios, huéspedes, contratos y pagos.");
    }
}
