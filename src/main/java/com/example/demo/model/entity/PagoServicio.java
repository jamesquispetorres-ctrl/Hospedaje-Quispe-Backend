package com.example.demo.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pago_servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servicio_adicional_id")
    private ServicioAdicional servicioAdicional;

    @Column(name = "nombre_servicio", nullable = false, length = 100)
    private String nombreServicio;

    @Column(name = "costo_cobrado", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoCobrado;
}
