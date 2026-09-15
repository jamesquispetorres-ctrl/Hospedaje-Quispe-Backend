package com.example.demo.repository;

import com.example.demo.model.entity.Habitacion;
import com.example.demo.model.enums.EstadoHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    Optional<Habitacion> findByNumero(String numero);

    boolean existsByNumero(String numero);

    boolean existsByNumeroAndIdNot(String numero, Long id);

    List<Habitacion> findByEstado(EstadoHabitacion estado);

    long countByEstado(EstadoHabitacion estado);

    List<Habitacion> findByOrderByPisoAscNumeroAsc();
}
