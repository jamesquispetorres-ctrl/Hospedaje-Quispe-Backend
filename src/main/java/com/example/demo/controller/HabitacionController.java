package com.example.demo.controller;

import com.example.demo.dto.HabitacionDTO;
import com.example.demo.model.enums.EstadoHabitacion;
import com.example.demo.service.HabitacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habitaciones")
@RequiredArgsConstructor
public class HabitacionController {

    private final HabitacionService habitacionService;

    @GetMapping
    public ResponseEntity<List<HabitacionDTO>> getAll() {
        return ResponseEntity.ok(habitacionService.getAllHabitaciones());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<HabitacionDTO>> getDisponibles() {
        return ResponseEntity.ok(habitacionService.getHabitacionesDisponibles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitacionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(habitacionService.getHabitacionById(id));
    }

    @PostMapping
    public ResponseEntity<HabitacionDTO> create(@Valid @RequestBody HabitacionDTO dto) {
        return new ResponseEntity<>(habitacionService.createHabitacion(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitacionDTO> update(@PathVariable Long id, @Valid @RequestBody HabitacionDTO dto) {
        return ResponseEntity.ok(habitacionService.updateHabitacion(id, dto));
    }

    // Endpoint directo para modificar únicamente el precio mensual de la habitación
    @PatchMapping("/{id}/precio")
    public ResponseEntity<HabitacionDTO> updatePrecio(@PathVariable Long id, @RequestBody Map<String, BigDecimal> payload) {
        BigDecimal nuevoPrecio = payload.get("precioMensual");
        return ResponseEntity.ok(habitacionService.updatePrecio(id, nuevoPrecio));
    }

    // Endpoint para cambiar el estado de la habitación (DISPONIBLE, OCUPADA, MANTENIMIENTO)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<HabitacionDTO> updateEstado(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        EstadoHabitacion estado = EstadoHabitacion.valueOf(payload.get("estado"));
        return ResponseEntity.ok(habitacionService.updateEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        habitacionService.deleteHabitacion(id);
        return ResponseEntity.noContent().build();
    }
}
