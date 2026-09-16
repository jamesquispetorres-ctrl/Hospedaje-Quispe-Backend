package com.example.demo.controller;

import com.example.demo.repository.HabitacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final HabitacionRepository habitacionRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("server", "UP");
        response.put("timestamp", LocalDateTime.now());

        try {
            long totalHabitaciones = habitacionRepository.count();
            response.put("database", "UP");
            response.put("message", "Servidor y base de datos activos");
            response.put("habitaciones_count", totalHabitaciones);
        } catch (Exception ex) {
            log.warn("Fallo temporal de conexion a la base de datos en /health: {}", ex.getMessage());
            response.put("database", "DOWN");
            response.put("message", "Servidor activo, pero la base de datos no respondio");
            response.put("error", ex.getMessage());
        }

        // Siempre retorna 200 OK para que los monitores externos (UptimeRobot, etc.) no marquen caida falsa
        return ResponseEntity.ok(response);
    }
}
