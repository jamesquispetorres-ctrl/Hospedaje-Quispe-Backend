package com.example.demo.config;

import com.example.demo.config.datasource.DatabaseType;
import com.example.demo.config.datasource.DbContextHolder;
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
    private final UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) {
        // 1. Inicializar datos en H2 (Modo Demo)
        try {
            DbContextHolder.setDatabaseType(DatabaseType.H2);
            log.info("Inicializando datos semilla en H2 (Modo Demo)...");
            poblarDatosSiEstaVacio(true);
        } catch (Exception e) {
            log.warn("No se pudo inicializar H2: {}", e.getMessage());
        } finally {
            DbContextHolder.clear();
        }

        // 2. Inicializar datos en MySQL (Producción) si la base de datos está disponible
        try {
            DbContextHolder.setDatabaseType(DatabaseType.MYSQL);
            log.info("Verificando datos en MySQL (Producción)...");
            poblarDatosSiEstaVacio(false);
        } catch (Exception e) {
            log.warn("MySQL no se encuentra accesible en este momento: {}. Se continuará con soporte H2.", e.getMessage());
        } finally {
            DbContextHolder.clear();
        }
    }

    private void poblarDatosSiEstaVacio(boolean esDemo) {
        // Sincronizar saldos de pagos
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
            log.debug("Aviso al sincronizar pagos: {}", e.getMessage());
        }

        // Crear usuarios iniciales
        try {
            if (esDemo) {
                if (!usuarioRepository.existsByUsernameIgnoreCase("demo_user")) {
                    usuarioRepository.save(Usuario.builder()
                            .username("demo_user")
                            .password("demo123")
                            .nombre("Administrador Demo")
                            .email("brianquispetorres@gmail.com")
                            .rol("Modo Demostración")
                            .activo(true)
                            .build());
                }
            } else {
                if (!usuarioRepository.existsByUsernameIgnoreCase("QuispeCastañeda")) {
                    usuarioRepository.save(Usuario.builder()
                            .username("QuispeCastañeda")
                            .password("qcasta14")
                            .nombre("Demofilo Quispe Castañeda")
                            .email("brianquispetorres@gmail.com")
                            .rol("Administrador General")
                            .activo(true)
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo crear usuario inicial: {}", e.getMessage());
        }

        if (habitacionRepository.count() > 0) {
            log.info("Datos ya cargados previamente para {}.", esDemo ? "H2 (Demo)" : "MySQL");
            return;
        }

        log.info("Cargando catálogo completo de habitaciones y contratos en {}...", esDemo ? "H2 (Demo)" : "MySQL");

        // 1. Habitaciones completas (Pisos 1, 2, 3 y 4)
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

        Habitacion hab103 = habitacionRepository.save(Habitacion.builder()
                .numero("103")
                .piso(1)
                .precioMensual(new BigDecimal("500.00"))
                .estado(EstadoHabitacion.DISPONIBLE)
                .descripcion("Piso 1, habitación doble con dos camas individuales, acceso cercano a recepción")
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
                .descripcion("Piso 2, amplia suite minera, escritorio de trabajo, baño con termoeléctrica")
                .build());

        Habitacion hab203 = habitacionRepository.save(Habitacion.builder()
                .numero("203")
                .piso(2)
                .precioMensual(new BigDecimal("520.00"))
                .estado(EstadoHabitacion.OCUPADA)
                .descripcion("Piso 2, matrimonial ejecutiva, balcón con vista al valle andino")
                .build());

        Habitacion hab301 = habitacionRepository.save(Habitacion.builder()
                .numero("301")
                .piso(3)
                .precioMensual(new BigDecimal("400.00"))
                .estado(EstadoHabitacion.MANTENIMIENTO)
                .descripcion("Piso 3, en mantenimiento preventivo de grifería y pintura")
                .build());

        Habitacion hab302 = habitacionRepository.save(Habitacion.builder()
                .numero("302")
                .piso(3)
                .precioMensual(new BigDecimal("490.00"))
                .estado(EstadoHabitacion.DISPONIBLE)
                .descripcion("Piso 3, individual moderna con escritorio y closet de madera")
                .build());

        Habitacion hab303 = habitacionRepository.save(Habitacion.builder()
                .numero("303")
                .piso(3)
                .precioMensual(new BigDecimal("600.00"))
                .estado(EstadoHabitacion.OCUPADA)
                .descripcion("Piso 3, matrimonial superior con cama king y juego de estar")
                .build());

        Habitacion hab401 = habitacionRepository.save(Habitacion.builder()
                .numero("401")
                .piso(4)
                .precioMensual(new BigDecimal("750.00"))
                .estado(EstadoHabitacion.DISPONIBLE)
                .descripcion("Piso 4, estudio independiente con kitchenette y vista panorámica a los cerros")
                .build());

        // 2. Servicios Adicionales
        ServicioAdicional servInternet = servicioRepository.save(ServicioAdicional.builder()
                .nombre("Internet Fibra Óptica Alta Velocidad")
                .tarifaFija(new BigDecimal("40.00"))
                .activo(true)
                .descripcion("Conexión dedicada de 200 Mbps para trabajo remoto y streaming")
                .build());

        ServicioAdicional servLuz = servicioRepository.save(ServicioAdicional.builder()
                .nombre("Consumo Eléctrico Fijo (Ducha y Termo)")
                .tarifaFija(new BigDecimal("35.00"))
                .activo(true)
                .descripcion("Tarifa plana mensual para calentador de agua caliente continuo")
                .build());

        ServicioAdicional servLavanderia = servicioRepository.save(ServicioAdicional.builder()
                .nombre("Servicio Semanal de Lavandería")
                .tarifaFija(new BigDecimal("30.00"))
                .activo(true)
                .descripcion("Lavado y secado semanal de prendas de trabajo y sábanas")
                .build());

        ServicioAdicional servCochera = servicioRepository.save(ServicioAdicional.builder()
                .nombre("Cochera y Estacionamiento Techado")
                .tarifaFija(new BigDecimal("50.00"))
                .activo(true)
                .descripcion("Espacio vigilado las 24 horas para camioneta o vehículo minero")
                .build());

        ServicioAdicional servLimpieza = servicioRepository.save(ServicioAdicional.builder()
                .nombre("Limpieza y Cambio de Toallas Interdiario")
                .tarifaFija(new BigDecimal("25.00"))
                .activo(true)
                .descripcion("Mantenimiento regular y desinfección de la habitación")
                .build());

        // 3. Huéspedes registrados
        Huesped h1 = huespedRepository.save(Huesped.builder()
                .dni("45892134")
                .nombres("Carlos Eduardo")
                .apellidos("Mendoza Vilca")
                .telefono("984123456")
                .email("carlos.mendoza@gmail.com")
                .activo(true)
                .build());

        Huesped h2 = huespedRepository.save(Huesped.builder()
                .dni("71245689")
                .nombres("Ana Patricia")
                .apellidos("Gómez Huamán")
                .telefono("951753951")
                .email("ana.gomez@gmail.com")
                .activo(true)
                .build());

        Huesped h3 = huespedRepository.save(Huesped.builder()
                .dni("10258963")
                .nombres("Roberto")
                .apellidos("Flores Mamani")
                .telefono("963258741")
                .email("roberto.flores@outlook.com")
                .activo(true)
                .build());

        Huesped h4 = huespedRepository.save(Huesped.builder()
                .dni("48956321")
                .nombres("Lucía Fernanda")
                .apellidos("Ramos Paredes")
                .telefono("978456123")
                .email("lucia.ramos@empresa-minera.com")
                .activo(true)
                .build());

        Huesped h5 = huespedRepository.save(Huesped.builder()
                .dni("42658974")
                .nombres("Miguel Ángel")
                .apellidos("Valenzuela Castro")
                .telefono("912345678")
                .email("m.valenzuela@contratistas.pe")
                .activo(true)
                .build());

        LocalDate hoy = LocalDate.now();

        // 4. Contratos de Alquiler
        Contrato c1 = contratoRepository.save(Contrato.builder()
                .huesped(h1)
                .habitacion(hab101)
                .fechaInicio(hoy.minusMonths(3))
                .diaPagoMensual(5)
                .montoMensualPactado(hab101.getPrecioMensual())
                .estado(EstadoContrato.ACTIVO)
                .observaciones("Contrato indefinido de alquiler minero con servicios incluidos")
                .build());

        Contrato c2 = contratoRepository.save(Contrato.builder()
                .huesped(h2)
                .habitacion(hab102)
                .fechaInicio(hoy.minusMonths(1))
                .diaPagoMensual(15)
                .montoMensualPactado(hab102.getPrecioMensual())
                .estado(EstadoContrato.ACTIVO)
                .observaciones("Ingeniera civil asignada a supervisión de mina")
                .build());

        Contrato c3 = contratoRepository.save(Contrato.builder()
                .huesped(h3)
                .habitacion(hab202)
                .fechaInicio(hoy.minusMonths(5))
                .diaPagoMensual(1)
                .montoMensualPactado(hab202.getPrecioMensual())
                .estado(EstadoContrato.ACTIVO)
                .observaciones("Residente temporal de operaciones metalúrgicas")
                .build());

        Contrato c4 = contratoRepository.save(Contrato.builder()
                .huesped(h4)
                .habitacion(hab203)
                .fechaInicio(hoy.minusMonths(2))
                .diaPagoMensual(10)
                .montoMensualPactado(hab203.getPrecioMensual())
                .estado(EstadoContrato.ACTIVO)
                .observaciones("Geóloga senior en campaña de exploración")
                .build());

        Contrato c5 = contratoRepository.save(Contrato.builder()
                .huesped(h5)
                .habitacion(hab303)
                .fechaInicio(hoy.minusMonths(4))
                .diaPagoMensual(20)
                .montoMensualPactado(hab303.getPrecioMensual())
                .estado(EstadoContrato.ACTIVO)
                .observaciones("Contratista de transporte de personal con cochera asignada")
                .build());

        // 5. Historial de Pagos y Cuotas
        // Pago Atrasado (Alerta en Dashboard)
        LocalDate vencimientoAtrasado = hoy.minusDays(6);
        Pago pagoAtrasado = Pago.builder()
                .contrato(c1)
                .periodoMesAnio(vencimientoAtrasado.getYear() + "-" + String.format("%02d", vencimientoAtrasado.getMonthValue()))
                .fechaVencimiento(vencimientoAtrasado)
                .montoHabitacion(c1.getMontoMensualPactado())
                .montoServicios(servInternet.getTarifaFija().add(servLuz.getTarifaFija()))
                .montoTotal(c1.getMontoMensualPactado().add(servInternet.getTarifaFija()).add(servLuz.getTarifaFija()))
                .montoPagado(BigDecimal.ZERO)
                .saldoPendiente(c1.getMontoMensualPactado().add(servInternet.getTarifaFija()).add(servLuz.getTarifaFija()))
                .estadoPago(EstadoPago.ATRASADO)
                .observaciones("Cuota vencida hace 6 días. Pendiente de coordinación.")
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

        // Pago Próximo a vencer (en 3 días)
        LocalDate vencimientoProximo = hoy.plusDays(3);
        Pago pagoProximo = Pago.builder()
                .contrato(c2)
                .periodoMesAnio(vencimientoProximo.getYear() + "-" + String.format("%02d", vencimientoProximo.getMonthValue()))
                .fechaVencimiento(vencimientoProximo)
                .montoHabitacion(c2.getMontoMensualPactado())
                .montoServicios(servInternet.getTarifaFija().add(servLavanderia.getTarifaFija()))
                .montoTotal(c2.getMontoMensualPactado().add(servInternet.getTarifaFija()).add(servLavanderia.getTarifaFija()))
                .montoPagado(BigDecimal.ZERO)
                .saldoPendiente(c2.getMontoMensualPactado().add(servInternet.getTarifaFija()).add(servLavanderia.getTarifaFija()))
                .estadoPago(EstadoPago.PENDIENTE)
                .observaciones("Cuota mensual próxima a vencer en 3 días.")
                .build();
        pagoProximo.addServicio(PagoServicio.builder()
                .servicioAdicional(servInternet)
                .nombreServicio(servInternet.getNombre())
                .costoCobrado(servInternet.getTarifaFija())
                .build());
        pagoProximo.addServicio(PagoServicio.builder()
                .servicioAdicional(servLavanderia)
                .nombreServicio(servLavanderia.getNombre())
                .costoCobrado(servLavanderia.getTarifaFija())
                .build());
        pagoRepository.save(pagoProximo);

        // Pago Pagado con YAPE
        LocalDate vencimientoPagado1 = hoy.minusMonths(1);
        Pago pagoCancelado1 = Pago.builder()
                .contrato(c3)
                .periodoMesAnio(vencimientoPagado1.getYear() + "-" + String.format("%02d", vencimientoPagado1.getMonthValue()))
                .fechaVencimiento(vencimientoPagado1)
                .fechaPago(vencimientoPagado1.minusDays(1))
                .montoHabitacion(c3.getMontoMensualPactado())
                .montoServicios(servLuz.getTarifaFija())
                .montoTotal(c3.getMontoMensualPactado().add(servLuz.getTarifaFija()))
                .montoPagado(c3.getMontoMensualPactado().add(servLuz.getTarifaFija()))
                .saldoPendiente(BigDecimal.ZERO)
                .metodoPago(MetodoPago.YAPE)
                .estadoPago(EstadoPago.PAGADO)
                .observaciones("Cancelado a tiempo vía Yape.")
                .build();
        pagoCancelado1.addServicio(PagoServicio.builder()
                .servicioAdicional(servLuz)
                .nombreServicio(servLuz.getNombre())
                .costoCobrado(servLuz.getTarifaFija())
                .build());
        pagoRepository.save(pagoCancelado1);

        // Pago Pagado con TRANSFERENCIA BANCARIA (BCP)
        LocalDate vencimientoPagado2 = hoy.minusDays(12);
        Pago pagoCancelado2 = Pago.builder()
                .contrato(c4)
                .periodoMesAnio(vencimientoPagado2.getYear() + "-" + String.format("%02d", vencimientoPagado2.getMonthValue()))
                .fechaVencimiento(vencimientoPagado2)
                .fechaPago(vencimientoPagado2)
                .montoHabitacion(c4.getMontoMensualPactado())
                .montoServicios(servInternet.getTarifaFija().add(servLimpieza.getTarifaFija()))
                .montoTotal(c4.getMontoMensualPactado().add(servInternet.getTarifaFija()).add(servLimpieza.getTarifaFija()))
                .montoPagado(c4.getMontoMensualPactado().add(servInternet.getTarifaFija()).add(servLimpieza.getTarifaFija()))
                .saldoPendiente(BigDecimal.ZERO)
                .metodoPago(MetodoPago.TRANSFERENCIA)
                .estadoPago(EstadoPago.PAGADO)
                .observaciones("Transferencia directa BCP confirmada con comprobante.")
                .build();
        pagoCancelado2.addServicio(PagoServicio.builder()
                .servicioAdicional(servInternet)
                .nombreServicio(servInternet.getNombre())
                .costoCobrado(servInternet.getTarifaFija())
                .build());
        pagoCancelado2.addServicio(PagoServicio.builder()
                .servicioAdicional(servLimpieza)
                .nombreServicio(servLimpieza.getNombre())
                .costoCobrado(servLimpieza.getTarifaFija())
                .build());
        pagoRepository.save(pagoCancelado2);

        // Pago con Cochera y Efectivo
        LocalDate vencimientoPagado3 = hoy.minusDays(20);
        Pago pagoCancelado3 = Pago.builder()
                .contrato(c5)
                .periodoMesAnio(vencimientoPagado3.getYear() + "-" + String.format("%02d", vencimientoPagado3.getMonthValue()))
                .fechaVencimiento(vencimientoPagado3)
                .fechaPago(vencimientoPagado3.minusDays(2))
                .montoHabitacion(c5.getMontoMensualPactado())
                .montoServicios(servCochera.getTarifaFija().add(servLuz.getTarifaFija()))
                .montoTotal(c5.getMontoMensualPactado().add(servCochera.getTarifaFija()).add(servLuz.getTarifaFija()))
                .montoPagado(c5.getMontoMensualPactado().add(servCochera.getTarifaFija()).add(servLuz.getTarifaFija()))
                .saldoPendiente(BigDecimal.ZERO)
                .metodoPago(MetodoPago.EFECTIVO)
                .estadoPago(EstadoPago.PAGADO)
                .observaciones("Cancelado en efectivo en recepción de hospedaje.")
                .build();
        pagoCancelado3.addServicio(PagoServicio.builder()
                .servicioAdicional(servCochera)
                .nombreServicio(servCochera.getNombre())
                .costoCobrado(servCochera.getTarifaFija())
                .build());
        pagoCancelado3.addServicio(PagoServicio.builder()
                .servicioAdicional(servLuz)
                .nombreServicio(servLuz.getNombre())
                .costoCobrado(servLuz.getTarifaFija())
                .build());
        pagoRepository.save(pagoCancelado3);

        log.info("¡Datos semilla completos cargados con éxito para {}!", esDemo ? "H2 (Demo)" : "MySQL");
    }
}
