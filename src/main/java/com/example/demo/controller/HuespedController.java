package com.example.demo.controller;

import com.example.demo.dto.HuespedDTO;
import com.example.demo.service.HuespedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/huespedes")
@RequiredArgsConstructor
public class HuespedController {

    private final HuespedService huespedService;

    @GetMapping
    public ResponseEntity<List<HuespedDTO>> getAll(@RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(huespedService.searchHuespedes(search));
        }
        return ResponseEntity.ok(huespedService.getAllHuespedes());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<HuespedDTO>> getActivos() {
        return ResponseEntity.ok(huespedService.getHuespedesActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HuespedDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(huespedService.getHuespedById(id));
    }

    @PostMapping
    public ResponseEntity<HuespedDTO> create(@Valid @RequestBody HuespedDTO dto) {
        return new ResponseEntity<>(huespedService.createHuesped(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HuespedDTO> update(@PathVariable Long id, @Valid @RequestBody HuespedDTO dto) {
        return ResponseEntity.ok(huespedService.updateHuesped(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        huespedService.deleteHuesped(id);
        return ResponseEntity.noContent().build();
    }
}
