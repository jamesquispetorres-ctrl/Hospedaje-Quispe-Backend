package com.example.demo.controller;

import com.example.demo.dto.ServicioAdicionalDTO;
import com.example.demo.service.ServicioAdicionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioAdicionalController {

    private final ServicioAdicionalService servicioService;

    @GetMapping
    public ResponseEntity<List<ServicioAdicionalDTO>> getAll() {
        return ResponseEntity.ok(servicioService.getAllServicios());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ServicioAdicionalDTO>> getActivos() {
        return ResponseEntity.ok(servicioService.getServiciosActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioAdicionalDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(servicioService.getServicioById(id));
    }

    @PostMapping
    public ResponseEntity<ServicioAdicionalDTO> create(@Valid @RequestBody ServicioAdicionalDTO dto) {
        return new ResponseEntity<>(servicioService.createServicio(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioAdicionalDTO> update(@PathVariable Long id, @Valid @RequestBody ServicioAdicionalDTO dto) {
        return ResponseEntity.ok(servicioService.updateServicio(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        servicioService.deleteServicio(id);
        return ResponseEntity.noContent().build();
    }
}
