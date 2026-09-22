package com.minierp.backend.controlador;

import com.minierp.backend.dto.LoginPeticion;
import com.minierp.backend.dto.LoginRespuesta;
import com.minierp.backend.dto.YoRespuesta;
import com.minierp.backend.modelo.Usuario;
import com.minierp.backend.seguridad.UsuarioPrincipal;
import com.minierp.backend.servicio.AutenticacionServicio;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionControlador {

    private final AutenticacionServicio autenticacionServicio;

    public AutenticacionControlador(AutenticacionServicio autenticacionServicio) {
        this.autenticacionServicio = autenticacionServicio;
    }

    @PostMapping("/login")
    public LoginRespuesta login(@Valid @RequestBody LoginPeticion peticion) {
        return autenticacionServicio.login(peticion.username(), peticion.password());
    }

    @GetMapping("/yo")
    public YoRespuesta yo(@AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario usuario = principal.getUsuario();
        return new YoRespuesta(usuario.getUsername(), usuario.getNombreCompleto(), usuario.getRol().getNombre());
    }
}
