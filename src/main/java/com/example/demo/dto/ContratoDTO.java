package com.example.demo.dto;

import com.example.demo.model.enums.EstadoContrato;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ContratoDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "El ID de huésped es obligatorio")
        private Long huespedId;

        @NotNull(message = "El ID de habitación es obligatorio")
        private Long habitacionId;

        @NotNull(message = "La fecha de inicio es obligatoria")
        private LocalDate fechaInicio;

        @NotNull(message = "El día de pago mensual es obligatorio (1-31)")
        @Min(value = 1, message = "El día debe estar entre 1 y 31")
        @Max(value = 31, message = "El día debe estar entre 1 y 31")
        private Integer diaPagoMensual;

        private BigDecimal montoMensualPactado;

        private String observaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long huespedId;
        private String huespedNombreCompleto;
        private String huespedDni;
        private String huespedTelefono;
        private Long habitacionId;
        private String habitacionNumero;
        private Integer habitacionPiso;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private Integer diaPagoMensual;
        private BigDecimal montoMensualPactado;
        private EstadoContrato estado;
        private String observaciones;
        private LocalDateTime fechaCreacion;
    }
}
