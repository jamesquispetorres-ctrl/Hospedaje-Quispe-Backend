package com.example.demo.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "servicios_adicionales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "La tarifa fija es obligatoria")
    @DecimalMin(value = "0.0", message = "La tarifa fija no puede ser negativa")
    @Column(name = "tarifa_fija", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaFija;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(length = 255)
    private String descripcion;

    @PrePersist
    public void prePersist() {
        if (this.activo == null) {
            this.activo = true;
        }
    }
}
