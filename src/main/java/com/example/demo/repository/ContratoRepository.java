package com.example.demo.repository;

import com.example.demo.model.entity.Contrato;
import com.example.demo.model.enums.EstadoContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    List<Contrato> findByEstado(EstadoContrato estado);

    List<Contrato> findByHuespedId(Long huespedId);

    List<Contrato> findByHabitacionId(Long habitacionId);

    Optional<Contrato> findByHabitacionIdAndEstado(Long habitacionId, EstadoContrato estado);

    boolean existsByHabitacionIdAndEstado(Long habitacionId, EstadoContrato estado);

    @Query("SELECT c FROM Contrato c ORDER BY c.fechaCreacion DESC")
    List<Contrato> findAllOrderByFechaCreacionDesc();
}
