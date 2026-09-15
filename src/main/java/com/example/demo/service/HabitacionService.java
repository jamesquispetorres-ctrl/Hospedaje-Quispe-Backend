package com.example.demo.service;

import com.example.demo.dto.HabitacionDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Habitacion;
import com.example.demo.model.entity.Contrato;
import com.example.demo.model.entity.Pago;
import com.example.demo.model.enums.EstadoHabitacion;
import com.example.demo.repository.ContratoRepository;
import com.example.demo.repository.HabitacionRepository;
import com.example.demo.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final ContratoRepository contratoRepository;
    private final PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    public List<HabitacionDTO> getAllHabitaciones() {
        return habitacionRepository.findByOrderByPisoAscNumeroAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HabitacionDTO> getHabitacionesDisponibles() {
        return habitacionRepository.findByEstado(EstadoHabitacion.DISPONIBLE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HabitacionDTO getHabitacionById(Long id) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada con ID: " + id));
        return toDTO(habitacion);
    }

    @Transactional
    public HabitacionDTO createHabitacion(HabitacionDTO dto) {
        if (habitacionRepository.existsByNumero(dto.getNumero())) {
            throw new BadRequestException("Ya existe una habitación registrada con el número: " + dto.getNumero());
        }

        Habitacion habitacion = Habitacion.builder()
                .numero(dto.getNumero().trim())
                .piso(dto.getPiso())
                .precioMensual(dto.getPrecioMensual())
                .estado(dto.getEstado() != null ? dto.getEstado() : EstadoHabitacion.DISPONIBLE)
                .descripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null)
                .build();

        Habitacion guardada = habitacionRepository.save(habitacion);
        return toDTO(guardada);
    }

    @Transactional
    public HabitacionDTO updateHabitacion(Long id, HabitacionDTO dto) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada con ID: " + id));

        if (habitacionRepository.existsByNumeroAndIdNot(dto.getNumero(), id)) {
            throw new BadRequestException("El número de habitación " + dto.getNumero() + " ya está en uso.");
        }

        habitacion.setNumero(dto.getNumero().trim());
        habitacion.setPiso(dto.getPiso());
        habitacion.setPrecioMensual(dto.getPrecioMensual());
        if (dto.getEstado() != null) {
            habitacion.setEstado(dto.getEstado());
        }
        habitacion.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);

        Habitacion actualizada = habitacionRepository.save(habitacion);
        return toDTO(actualizada);
    }

    @Transactional
    public HabitacionDTO updatePrecio(Long id, BigDecimal nuevoPrecio) {
        if (nuevoPrecio == null || nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El nuevo precio mensual debe ser mayor a 0");
        }
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada con ID: " + id));
        habitacion.setPrecioMensual(nuevoPrecio);
        return toDTO(habitacionRepository.save(habitacion));
    }

    @Transactional
    public HabitacionDTO updateEstado(Long id, EstadoHabitacion nuevoEstado) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada con ID: " + id));
        habitacion.setEstado(nuevoEstado);
        return toDTO(habitacionRepository.save(habitacion));
    }

    @Transactional
    public void deleteHabitacion(Long id) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada con ID: " + id));

        // 1. Eliminar primero los contratos y pagos asociados a esta habitación
        List<Contrato> contratos = contratoRepository.findByHabitacionId(id);
        if (contratos != null && !contratos.isEmpty()) {
            for (Contrato c : contratos) {
                List<Pago> pagos = pagoRepository.findByContratoIdOrderByFechaVencimientoDesc(c.getId());
                if (pagos != null && !pagos.isEmpty()) {
                    pagoRepository.deleteAll(pagos);
                }
                contratoRepository.delete(c);
            }
        }

        // 2. Eliminar la habitación
        habitacionRepository.delete(habitacion);
    }

    public HabitacionDTO toDTO(Habitacion entity) {
        return HabitacionDTO.builder()
                .id(entity.getId())
                .numero(entity.getNumero())
                .piso(entity.getPiso())
                .precioMensual(entity.getPrecioMensual())
                .estado(entity.getEstado())
                .descripcion(entity.getDescripcion())
                .build();
    }
}
