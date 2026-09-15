package com.example.demo.controller;

import com.example.demo.dto.PagoDTO;
import com.example.demo.model.enums.MetodoPago;
import com.example.demo.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping
    public ResponseEntity<List<PagoDTO.Response>> getAll() {
        return ResponseEntity.ok(pagoService.getAllPagos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.getPagoById(id));
    }

    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<List<PagoDTO.Response>> getByContrato(@PathVariable Long contratoId) {
        return ResponseEntity.ok(pagoService.getPagosByContrato(contratoId));
    }

    // Registrar nueva mensualidad mediante JSON
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoDTO.Response> createJson(@Valid @RequestBody PagoDTO.Request request) {
        return new ResponseEntity<>(pagoService.createPago(request, null), HttpStatus.CREATED);
    }

    // Registrar nueva mensualidad con archivo de comprobante adjunto (multipart/form-data)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PagoDTO.Response> createMultipart(
            @RequestPart("data") @Valid PagoDTO.Request request,
            @RequestPart(value = "comprobante", required = false) MultipartFile comprobante) {
        return new ResponseEntity<>(pagoService.createPago(request, comprobante), HttpStatus.CREATED);
    }

    // Marcar como pagado o abonar pago en partes
    @PostMapping(value = "/{id}/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PagoDTO.Response> registrarPago(
            @PathVariable Long id,
            @RequestParam("metodoPago") MetodoPago metodoPago,
            @RequestParam(value = "fechaPago", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPago,
            @RequestParam(value = "montoAbonado", required = false) BigDecimal montoAbonado,
            @RequestParam(value = "serviciosIds", required = false) List<Long> serviciosIds,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestPart(value = "comprobante", required = false) MultipartFile comprobante) {

        return ResponseEntity.ok(pagoService.registrarPagoEfectuado(
                id, metodoPago, fechaPago, montoAbonado, serviciosIds, observaciones, comprobante));
    }

    // Registrar pago adelantado de 1, 2, 3 o más meses (JSON)
    @PostMapping(value = "/adelantado", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PagoDTO.Response>> registrarAdelantadoJson(
            @Valid @RequestBody PagoDTO.AdelantadoRequest request) {
        return new ResponseEntity<>(pagoService.registrarPagoAdelantadoMultiMes(request, null), HttpStatus.CREATED);
    }

    // Registrar pago adelantado de varios meses con comprobante adjunto (multipart)
    @PostMapping(value = "/adelantado", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<PagoDTO.Response>> registrarAdelantadoMultipart(
            @RequestPart("data") @Valid PagoDTO.AdelantadoRequest request,
            @RequestPart(value = "comprobante", required = false) MultipartFile comprobante) {
        return new ResponseEntity<>(pagoService.registrarPagoAdelantadoMultiMes(request, comprobante), HttpStatus.CREATED);
    }
}
