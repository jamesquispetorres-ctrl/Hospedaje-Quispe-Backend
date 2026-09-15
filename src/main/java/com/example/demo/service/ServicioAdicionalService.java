package com.example.demo.service;

import com.example.demo.dto.ServicioAdicionalDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.ServicioAdicional;
import com.example.demo.repository.ServicioAdicionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioAdicionalService {

    private final ServicioAdicionalRepository servicioRepository;

    @Transactional(readOnly = true)
    public List<ServicioAdicionalDTO> getAllServicios() {
        return servicioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServicioAdicionalDTO> getServiciosActivos() {
        return servicioRepository.findByActivoTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServicioAdicionalDTO getServicioById(Long id) {
        ServicioAdicional servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));
        return toDTO(servicio);
    }

    @Transactional
    public ServicioAdicionalDTO createServicio(ServicioAdicionalDTO dto) {
        ServicioAdicional servicio = ServicioAdicional.builder()
                .nombre(dto.getNombre().trim())
                .tarifaFija(dto.getTarifaFija())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .descripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null)
                .build();

        ServicioAdicional guardado = servicioRepository.save(servicio);
        return toDTO(guardado);
    }

    @Transactional
    public ServicioAdicionalDTO updateServicio(Long id, ServicioAdicionalDTO dto) {
        ServicioAdicional servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));

        servicio.setNombre(dto.getNombre().trim());
        servicio.setTarifaFija(dto.getTarifaFija());
        if (dto.getActivo() != null) {
            servicio.setActivo(dto.getActivo());
        }
        servicio.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);

        ServicioAdicional actualizado = servicioRepository.save(servicio);
        return toDTO(actualizado);
    }

    @Transactional
    public void deleteServicio(Long id) {
        ServicioAdicional servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));
        servicio.setActivo(false);
        servicioRepository.save(servicio);
    }

    public ServicioAdicionalDTO toDTO(ServicioAdicional entity) {
        return ServicioAdicionalDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .tarifaFija(entity.getTarifaFija())
                .activo(entity.getActivo())
                .descripcion(entity.getDescripcion())
                .build();
    }
}
