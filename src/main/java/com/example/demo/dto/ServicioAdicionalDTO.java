package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioAdicionalDTO {

    private Long id;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    private String nombre;

    @NotNull(message = "La tarifa fija es obligatoria")
    @DecimalMin(value = "0.0", message = "La tarifa fija no puede ser negativa")
    private BigDecimal tarifaFija;

    private Boolean activo;

    private String descripcion;
}
