package com.example.demo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private boolean success;
    private String mensaje;
    private String token;
    private String nombre;
    private String usuario;
    private String email;
    private String rol;
    private boolean isDemo;
    private String avatarUrl;
}
