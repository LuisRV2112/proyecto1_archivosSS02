package com.minierp.backend.controlador;

import com.minierp.backend.dto.RolRespuesta;
import com.minierp.backend.dto.UsuarioActualizacionPeticion;
import com.minierp.backend.dto.UsuarioPeticion;
import com.minierp.backend.dto.UsuarioRespuesta;
import com.minierp.backend.seguridad.UsuarioPrincipal;
import com.minierp.backend.servicio.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Solo ADMINISTRACION (SecurityConfig, /api/usuarios/**)
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    public List<UsuarioRespuesta> listar() {
        return usuarioServicio.listar();
    }

    @GetMapping("/roles")
    public List<RolRespuesta> roles() {
        return usuarioServicio.listarRoles();
    }

    @PostMapping
    public UsuarioRespuesta crear(
            @Valid @RequestBody UsuarioPeticion peticion, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return usuarioServicio.crear(peticion, principal.getUsuario());
    }

    @PutMapping("/{id}")
    public UsuarioRespuesta actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioActualizacionPeticion peticion,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return usuarioServicio.actualizar(id, peticion, principal.getUsuario());
    }

    @PatchMapping("/{id}/activar")
    public UsuarioRespuesta activar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return usuarioServicio.activar(id, principal.getUsuario());
    }

    @PatchMapping("/{id}/desactivar")
    public UsuarioRespuesta desactivar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return usuarioServicio.desactivar(id, principal.getUsuario());
    }
}
