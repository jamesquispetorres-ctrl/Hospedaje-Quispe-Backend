package com.example.demo.repository;

import com.example.demo.model.entity.Pago;
import com.example.demo.model.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByContratoIdOrderByFechaVencimientoDesc(Long contratoId);

    java.util.Optional<Pago> findByContratoIdAndPeriodoMesAnio(Long contratoId, String periodoMesAnio);

    List<Pago> findByEstadoPago(EstadoPago estadoPago);

    @Query("SELECT p FROM Pago p " +
           "JOIN FETCH p.contrato c " +
           "JOIN FETCH c.huesped h " +
           "JOIN FETCH c.habitacion hab " +
           "WHERE p.estadoPago != 'PAGADO' AND p.fechaVencimiento < :hoy " +
           "ORDER BY p.fechaVencimiento ASC")
    List<Pago> findPagosAtrasados(@Param("hoy") LocalDate hoy);

    @Query("SELECT p FROM Pago p " +
           "JOIN FETCH p.contrato c " +
           "JOIN FETCH c.huesped h " +
           "JOIN FETCH c.habitacion hab " +
           "WHERE p.estadoPago != 'PAGADO' AND p.fechaVencimiento BETWEEN :desde AND :hasta " +
           "ORDER BY p.fechaVencimiento ASC")
    List<Pago> findPagosProximosVencer(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("SELECT p FROM Pago p " +
           "JOIN FETCH p.contrato c " +
           "JOIN FETCH c.huesped h " +
           "JOIN FETCH c.habitacion hab " +
           "ORDER BY p.fechaVencimiento DESC")
    List<Pago> findAllWithDetails();
}
