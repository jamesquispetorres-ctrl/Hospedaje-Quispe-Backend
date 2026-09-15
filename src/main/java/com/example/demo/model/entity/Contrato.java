package com.example.demo.model.entity;

import com.example.demo.model.enums.EstadoContrato;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contratos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El huésped es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "huesped_id", nullable = false)
    private Huesped huesped;

    @NotNull(message = "La habitación es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @NotNull(message = "El día de pago mensual es obligatorio")
    @Min(value = 1, message = "El día de pago debe ser entre 1 y 31")
    @Max(value = 31, message = "El día de pago debe ser entre 1 y 31")
    @Column(name = "dia_pago_mensual", nullable = false)
    private Integer diaPagoMensual;

    @NotNull(message = "El monto mensual pactado es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto mensual pactado debe ser mayor a 0")
    @Column(name = "monto_mensual_pactado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoMensualPactado;

    @NotNull(message = "El estado del contrato es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoContrato estado = EstadoContrato.ACTIVO;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoContrato.ACTIVO;
        }
    }
}
