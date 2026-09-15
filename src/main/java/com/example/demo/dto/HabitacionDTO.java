package com.example.demo.dto;

import com.example.demo.model.enums.EstadoHabitacion;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitacionDTO {

    private Long id;

    @NotBlank(message = "El número de habitación es obligatorio")
    private String numero;

    @NotNull(message = "El piso es obligatorio")
    @Min(value = 1, message = "El piso debe ser al menos 1")
    private Integer piso;

    @NotNull(message = "El precio mensual es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio mensual debe ser mayor a 0")
    private BigDecimal precioMensual;

    private EstadoHabitacion estado;

    private String descripcion;
}
