package com.example.demo.controller;

import com.example.demo.dto.ContratoDTO;
import com.example.demo.service.ContratoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping
    public ResponseEntity<List<ContratoDTO.Response>> getAll() {
        return ResponseEntity.ok(contratoService.getAllContratos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ContratoDTO.Response>> getActivos() {
        return ResponseEntity.ok(contratoService.getContratosActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratoDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contratoService.getContratoById(id));
    }

    @PostMapping
    public ResponseEntity<ContratoDTO.Response> create(@Valid @RequestBody ContratoDTO.Request request) {
        return new ResponseEntity<>(contratoService.createContrato(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<ContratoDTO.Response> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(contratoService.finalizarContrato(id));
    }
}
