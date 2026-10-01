package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.model.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String user = request.getUsuario() != null ? request.getUsuario().trim() : "";
        String pass = request.getClave() != null ? request.getClave().trim() : "";

        // Credenciales oficiales solicitadas: QuispeCastañeda / qcasta14
        boolean matchesFixedAdmin = 
                (user.equalsIgnoreCase("QuispeCastañeda") || user.equalsIgnoreCase("QuispeCastaneda") || user.equalsIgnoreCase("brianquispetorres@gmail.com"))
                && pass.equals("qcasta14");

        // O verificación en base de datos si existe el usuario registrado
        Optional<Usuario> usuarioDb = usuarioRepository.findByUsernameIgnoreCase(user);
        if (usuarioDb.isEmpty()) {
            usuarioDb = usuarioRepository.findByEmailIgnoreCase(user);
        }

        boolean matchesDb = usuarioDb.isPresent() 
                && Boolean.TRUE.equals(usuarioDb.get().getActivo())
                && pass.equals(usuarioDb.get().getPassword());

        if (matchesFixedAdmin || matchesDb) {
            String nombre = usuarioDb.map(Usuario::getNombre).orElse("Demofilo Quispe Castañeda");
            String username = usuarioDb.map(Usuario::getUsername).orElse("QuispeCastañeda");
            String email = usuarioDb.map(Usuario::getEmail).orElse("brianquispetorres@gmail.com");
            String rol = usuarioDb.map(Usuario::getRol).orElse("Administrador General");

            AuthResponse response = AuthResponse.builder()
                    .success(true)
                    .mensaje("Autenticación exitosa en Base de Datos Real")
                    .token("prod-auth-token-" + System.currentTimeMillis())
                    .nombre(nombre)
                    .usuario(username)
                    .email(email)
                    .rol(rol)
                    .isDemo(false)
                    .avatarUrl("/logo-gestiona-hospedaje.jpg")
                    .build();

            log.info("Inicio de sesión exitoso para usuario: {}", username);
            return ResponseEntity.ok(response);
        }

        log.warn("Intento de inicio de sesión fallido para: {}", user);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                AuthResponse.builder()
                        .success(false)
                        .mensaje("Credenciales incorrectas. Verifique su usuario y contraseña.")
                        .build()
        );
    }

    @PostMapping("/demo")
    public ResponseEntity<AuthResponse> loginDemo() {
        AuthResponse response = AuthResponse.builder()
                .success(true)
                .mensaje("Acceso Demo H2 activado")
                .token("demo-h2-token-" + System.currentTimeMillis())
                .nombre("Administrador Demo")
                .usuario("demo_user")
                .email("brianquispetorres@gmail.com")
                .rol("Modo Demostración (H2)")
                .isDemo(true)
                .avatarUrl("/demo-avatar.jpg")
                .build();

        log.info("Sesión Demo H2 activada");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/demo")
    public ResponseEntity<AuthResponse> getDemoInfo() {
        return loginDemo();
    }
}
