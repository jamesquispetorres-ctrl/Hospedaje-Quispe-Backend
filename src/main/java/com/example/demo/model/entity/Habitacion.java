package com.example.demo.model.entity;

import com.example.demo.model.enums.EstadoHabitacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "habitaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El número de habitación es obligatorio")
    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @NotNull(message = "El piso es obligatorio")
    @Min(value = 1, message = "El piso debe ser mayor o igual a 1")
    @Column(nullable = false)
    private Integer piso;

    @NotNull(message = "El precio mensual es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio mensual debe ser mayor a 0")
    @Column(name = "precio_mensual", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMensual;

    @NotNull(message = "El estado de la habitación es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoHabitacion estado = EstadoHabitacion.DISPONIBLE;

    @Column(length = 255)
    private String descripcion;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoHabitacion.DISPONIBLE;
        }
    }
}
