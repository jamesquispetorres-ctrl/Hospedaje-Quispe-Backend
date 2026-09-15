package com.example.demo.dto;

import com.example.demo.model.enums.EstadoPago;
import com.example.demo.model.enums.MetodoPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PagoDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "El contrato es obligatorio")
        private Long contratoId;

        @NotBlank(message = "El periodo es obligatorio (ej. 2026-03)")
        private String periodoMesAnio;

        @NotNull(message = "La fecha de vencimiento es obligatoria")
        private LocalDate fechaVencimiento;

        private LocalDate fechaPago;

        private BigDecimal montoHabitacion;

        private BigDecimal montoAbonado;

        @Builder.Default
        private List<Long> serviciosIds = new ArrayList<>();

        private MetodoPago metodoPago;

        private EstadoPago estadoPago;

        private String observaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdelantadoRequest {
        @NotNull(message = "El contrato es obligatorio")
        private Long contratoId;

        @NotBlank(message = "El mes de inicio es obligatorio (ej. 2026-09)")
        private String mesInicio;

        @NotNull(message = "La cantidad de meses es obligatoria")
        private Integer cantidadMeses;

        @Builder.Default
        private List<Long> serviciosIds = new ArrayList<>();

        private MetodoPago metodoPago;

        private LocalDate fechaPago;

        private String observaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServicioCobradoDTO {
        private Long id;
        private Long servicioAdicionalId;
        private String nombreServicio;
        private BigDecimal costoCobrado;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long contratoId;
        private Long huespedId;
        private String huespedNombreCompleto;
        private String huespedDni;
        private String huespedTelefono;
        private String habitacionNumero;
        private Integer habitacionPiso;
        private String periodoMesAnio;
        private LocalDate fechaVencimiento;
        private LocalDate fechaPago;
        private BigDecimal montoHabitacion;
        private BigDecimal montoServicios;
        private BigDecimal montoTotal;
        private BigDecimal montoPagado;
        private BigDecimal saldoPendiente;
        private MetodoPago metodoPago;
        private EstadoPago estadoPago;
        private String comprobanteUrl;
        private String observaciones;
        @Builder.Default
        private List<ServicioCobradoDTO> servicios = new ArrayList<>();
        private LocalDateTime fechaRegistro;
    }
}
