package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HuespedDTO {

    private Long id;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 12, message = "El DNI debe tener entre 8 y 12 caracteres")
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+ ]{9,15}$", message = "El formato de teléfono no es válido")
    private String telefono;

    private String email;

    private Boolean activo;

    private LocalDateTime fechaRegistro;
}
