package com.example.demo.service;

import com.example.demo.dto.DashboardDTO;
import com.example.demo.model.entity.Habitacion;
import com.example.demo.model.entity.Huesped;
import com.example.demo.model.entity.Pago;
import com.example.demo.model.enums.EstadoHabitacion;
import com.example.demo.model.enums.EstadoPago;
import com.example.demo.repository.HabitacionRepository;
import com.example.demo.repository.HuespedRepository;
import com.example.demo.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final HabitacionRepository habitacionRepository;
    private final HuespedRepository huespedRepository;
    private final PagoRepository pagoRepository;

    @Transactional
    public DashboardDTO getDashboardData() {
        LocalDate hoy = LocalDate.now();

        long totalHabitaciones = habitacionRepository.count();
        long ocupadas = habitacionRepository.countByEstado(EstadoHabitacion.OCUPADA);
        long disponibles = habitacionRepository.countByEstado(EstadoHabitacion.DISPONIBLE);
        long mantenimiento = habitacionRepository.countByEstado(EstadoHabitacion.MANTENIMIENTO);

        double tasaOcupacion = totalHabitaciones > 0
                ? Math.round(((double) ocupadas / totalHabitaciones) * 1000.0) / 10.0
                : 0.0;

        long totalHuespedesActivos = huespedRepository.findByActivoTrue().size();

        // 1. Alertas de Pagos Atrasados (vencimiento anterior a hoy y no pagados)
        List<Pago> pagosAtrasados = pagoRepository.findPagosAtrasados(hoy);
        List<DashboardDTO.AlertaPagoDTO> alertasAtrasadas = new ArrayList<>();
        BigDecimal totalMontoAtrasado = BigDecimal.ZERO;

        for (Pago p : pagosAtrasados) {
            // Actualizar a estado ATRASADO solo si estaba en PENDIENTE (mantener PARCIAL si ya hubo abono)
            if (p.getEstadoPago() == EstadoPago.PENDIENTE) {
                p.setEstadoPago(EstadoPago.ATRASADO);
                pagoRepository.save(p);
            }

            BigDecimal saldoAdeudado = (p.getSaldoPendiente() != null && p.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0)
                    ? p.getSaldoPendiente()
                    : p.getMontoTotal();

            totalMontoAtrasado = totalMontoAtrasado.add(saldoAdeudado);
            long diasMora = ChronoUnit.DAYS.between(p.getFechaVencimiento(), hoy);

            alertasAtrasadas.add(buildAlertaDTO(p, diasMora, true));
        }

        // 2. Alertas de Vencimientos Próximos (vencimiento en los siguientes 5 días)
        LocalDate limiteProximo = hoy.plusDays(5);
        List<Pago> pagosProximos = pagoRepository.findPagosProximosVencer(hoy, limiteProximo);
        List<DashboardDTO.AlertaPagoDTO> alertasProximas = new ArrayList<>();

        for (Pago p : pagosProximos) {
            long diasRestantes = ChronoUnit.DAYS.between(hoy, p.getFechaVencimiento());
            alertasProximas.add(buildAlertaDTO(p, diasRestantes, false));
        }

        return DashboardDTO.builder()
                .totalHabitaciones(totalHabitaciones)
                .habitacionesOcupadas(ocupadas)
                .habitacionesDisponibles(disponibles)
                .habitacionesMantenimiento(mantenimiento)
                .tasaOcupacion(tasaOcupacion)
                .totalHuespedesActivos(totalHuespedesActivos)
                .totalPagosAtrasados((long) alertasAtrasadas.size())
                .montoTotalAtrasado(totalMontoAtrasado)
                .totalProximosVencimientos((long) alertasProximas.size())
                .alertasAtrasadas(alertasAtrasadas)
                .alertasProximas(alertasProximas)
                .build();
    }

    private DashboardDTO.AlertaPagoDTO buildAlertaDTO(Pago pago, long diasDiferencia, boolean esAtrasado) {
        Huesped h = pago.getContrato().getHuesped();
        Habitacion hab = pago.getContrato().getHabitacion();

        String telefonoLimpio = limpiarTelefonoPeruano(h.getTelefono());
        String linkLlamada = "tel:+" + telefonoLimpio;

        String mensajeWhatsapp;
        if (esAtrasado) {
            mensajeWhatsapp = String.format(
                    "Hola %s, le saludamos de Hospedaje Quispe. Le recordamos que su mensualidad de S/. %.2f de la habitación %s (Periodo: %s) venció hace %d días (el %s). Por favor coordinar su regularización a la brevedad. ¡Gracias!",
                    h.getNombres(),
                    pago.getMontoTotal(),
                    hab.getNumero(),
                    pago.getPeriodoMesAnio(),
                    diasDiferencia,
                    pago.getFechaVencimiento()
            );
        } else {
            String cuando = (diasDiferencia == 0) ? "HOY" : ("en " + diasDiferencia + " días (el " + pago.getFechaVencimiento() + ")");
            mensajeWhatsapp = String.format(
                    "Hola %s, le saludamos de Hospedaje Quispe. Le recordamos que su mensualidad de S/. %.2f de la habitación %s (Periodo: %s) vence %s. Que tenga buen día.",
                    h.getNombres(),
                    pago.getMontoTotal(),
                    hab.getNumero(),
                    pago.getPeriodoMesAnio(),
                    cuando
            );
        }

        String linkWhatsapp = "https://wa.me/" + telefonoLimpio + "?text=" + URLEncoder.encode(mensajeWhatsapp, StandardCharsets.UTF_8);

        return DashboardDTO.AlertaPagoDTO.builder()
                .pagoId(pago.getId())
                .contratoId(pago.getContrato().getId())
                .huespedId(h.getId())
                .huespedNombre(h.getNombres() + " " + h.getApellidos())
                .huespedTelefono(h.getTelefono())
                .habitacionNumero(hab.getNumero())
                .piso(hab.getPiso())
                .periodo(pago.getPeriodoMesAnio())
                .fechaVencimiento(pago.getFechaVencimiento())
                .montoTotal(pago.getMontoTotal())
                .diasDiferencia(diasDiferencia)
                .esAtrasado(esAtrasado)
                .linkLlamada(linkLlamada)
                .linkWhatsapp(linkWhatsapp)
                .build();
    }

    private String limpiarTelefonoPeruano(String telefono) {
        if (telefono == null) return "51900000000";
        String soloDigitos = telefono.replaceAll("[^0-9]", "");
        if (soloDigitos.startsWith("51") && soloDigitos.length() >= 11) {
            return soloDigitos;
        }
        if (soloDigitos.length() == 9) {
            return "51" + soloDigitos;
        }
        return "51" + soloDigitos;
    }
}
