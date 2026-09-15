package com.example.demo.model.entity;

import com.example.demo.model.enums.EstadoPago;
import com.example.demo.model.enums.MetodoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El contrato es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @NotBlank(message = "El periodo es obligatorio (ej. 2026-03)")
    @Column(name = "periodo_mes_anio", nullable = false, length = 20)
    private String periodoMesAnio;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @NotNull(message = "El monto de habitación es obligatorio")
    @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
    @Column(name = "monto_habitacion", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoHabitacion;

    @NotNull(message = "El monto de servicios es obligatorio")
    @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
    @Column(name = "monto_servicios", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montoServicios = BigDecimal.ZERO;

    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto total debe ser mayor a 0")
    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @NotNull(message = "El monto pagado es obligatorio")
    @DecimalMin(value = "0.0", message = "El monto pagado no puede ser negativo")
    @Column(name = "monto_pagado", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montoPagado = BigDecimal.ZERO;

    @NotNull(message = "El saldo pendiente es obligatorio")
    @DecimalMin(value = "0.0", message = "El saldo pendiente no puede ser negativo")
    @Column(name = "saldo_pendiente", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal saldoPendiente = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", length = 30)
    private MetodoPago metodoPago;

    @NotNull(message = "El estado de pago es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 20)
    @Builder.Default
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @Column(name = "comprobante_url", length = 300)
    private String comprobanteUrl;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PagoServicio> servicios = new ArrayList<>();

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
        if (this.estadoPago == null) {
            this.estadoPago = EstadoPago.PENDIENTE;
        }
        if (this.montoServicios == null) {
            this.montoServicios = BigDecimal.ZERO;
        }
        if (this.montoPagado == null) {
            this.montoPagado = (this.estadoPago == EstadoPago.PAGADO) ? this.montoTotal : BigDecimal.ZERO;
        }
        if (this.saldoPendiente == null) {
            this.saldoPendiente = (this.montoTotal != null && this.montoPagado != null) 
                    ? this.montoTotal.subtract(this.montoPagado).max(BigDecimal.ZERO) 
                    : BigDecimal.ZERO;
        }
    }

    public void addServicio(PagoServicio servicio) {
        servicios.add(servicio);
        servicio.setPago(this);
    }

    public void removeServicio(PagoServicio servicio) {
        servicios.remove(servicio);
        servicio.setPago(null);
    }
}
