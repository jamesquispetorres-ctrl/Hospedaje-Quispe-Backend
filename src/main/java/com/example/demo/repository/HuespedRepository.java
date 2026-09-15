package com.example.demo.repository;

import com.example.demo.model.entity.Huesped;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HuespedRepository extends JpaRepository<Huesped, Long> {

    Optional<Huesped> findByDni(String dni);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Long id);

    List<Huesped> findByActivoTrue();

    @Query("SELECT h FROM Huesped h WHERE " +
           "LOWER(h.nombres) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.apellidos) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "h.dni LIKE CONCAT('%', :query, '%') OR " +
           "h.telefono LIKE CONCAT('%', :query, '%')")
    List<Huesped> search(@Param("query") String query);
}
