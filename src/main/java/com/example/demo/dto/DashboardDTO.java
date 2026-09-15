package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    private Long totalHabitaciones;
    private Long habitacionesOcupadas;
    private Long habitacionesDisponibles;
    private Long habitacionesMantenimiento;
    private Double tasaOcupacion; // porcentaje de 0 a 100

    private Long totalHuespedesActivos;
    private Long totalPagosAtrasados;
    private BigDecimal montoTotalAtrasado;
    private Long totalProximosVencimientos;

    @Builder.Default
    private List<AlertaPagoDTO> alertasAtrasadas = new ArrayList<>();

    @Builder.Default
    private List<AlertaPagoDTO> alertasProximas = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlertaPagoDTO {
        private Long pagoId;
        private Long contratoId;
        private Long huespedId;
        private String huespedNombre;
        private String huespedTelefono;
        private String habitacionNumero;
        private Integer piso;
        private String periodo;
        private LocalDate fechaVencimiento;
        private BigDecimal montoTotal;
        private Long diasDiferencia; // Si es atrasado: días transcurridos de mora. Si es próximo: días restantes.
        private Boolean esAtrasado;
        private String linkLlamada;  // Formato: tel:+51987654321
        private String linkWhatsapp; // Formato: https://wa.me/51987654321?text=...
    }
}
