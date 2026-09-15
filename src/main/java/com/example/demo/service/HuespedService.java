package com.example.demo.service;

import com.example.demo.dto.HuespedDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Huesped;
import com.example.demo.repository.HuespedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HuespedService {

    private final HuespedRepository huespedRepository;

    @Transactional(readOnly = true)
    public List<HuespedDTO> getAllHuespedes() {
        return huespedRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HuespedDTO> getHuespedesActivos() {
        return huespedRepository.findByActivoTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HuespedDTO getHuespedById(Long id) {
        Huesped huesped = huespedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Huésped no encontrado con ID: " + id));
        return toDTO(huesped);
    }

    @Transactional
    public HuespedDTO createHuesped(HuespedDTO dto) {
        if (huespedRepository.existsByDni(dto.getDni())) {
            throw new BadRequestException("Ya existe un huésped registrado con el DNI: " + dto.getDni());
        }

        Huesped huesped = Huesped.builder()
                .dni(dto.getDni().trim())
                .nombres(dto.getNombres().trim())
                .apellidos(dto.getApellidos().trim())
                .telefono(dto.getTelefono().trim())
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();

        Huesped guardado = huespedRepository.save(huesped);
        return toDTO(guardado);
    }

    @Transactional
    public HuespedDTO updateHuesped(Long id, HuespedDTO dto) {
        Huesped huesped = huespedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Huésped no encontrado con ID: " + id));

        if (huespedRepository.existsByDniAndIdNot(dto.getDni(), id)) {
            throw new BadRequestException("El DNI " + dto.getDni() + " ya pertenece a otro huésped.");
        }

        huesped.setDni(dto.getDni().trim());
        huesped.setNombres(dto.getNombres().trim());
        huesped.setApellidos(dto.getApellidos().trim());
        huesped.setTelefono(dto.getTelefono().trim());
        huesped.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        if (dto.getActivo() != null) {
            huesped.setActivo(dto.getActivo());
        }

        Huesped actualizado = huespedRepository.save(huesped);
        return toDTO(actualizado);
    }

    @Transactional
    public void deleteHuesped(Long id) {
        Huesped huesped = huespedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Huésped no encontrado con ID: " + id));
        huesped.setActivo(false); // Eliminación lógica
        huespedRepository.save(huesped);
    }

    @Transactional(readOnly = true)
    public List<HuespedDTO> searchHuespedes(String query) {
        if (query == null || query.isBlank()) {
            return getAllHuespedes();
        }
        return huespedRepository.search(query.trim()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public HuespedDTO toDTO(Huesped entity) {
        return HuespedDTO.builder()
                .id(entity.getId())
                .dni(entity.getDni())
                .nombres(entity.getNombres())
                .apellidos(entity.getApellidos())
                .telefono(entity.getTelefono())
                .email(entity.getEmail())
                .activo(entity.getActivo())
                .fechaRegistro(entity.getFechaRegistro())
                .build();
    }
}
