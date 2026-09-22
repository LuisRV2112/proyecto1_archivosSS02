package com.minierp.backend.controlador;

import com.minierp.backend.dto.ClientePeticion;
import com.minierp.backend.dto.ClienteRespuesta;
import com.minierp.backend.seguridad.UsuarioPrincipal;
import com.minierp.backend.servicio.ClienteServicio;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteControlador {

    private final ClienteServicio clienteServicio;

    public ClienteControlador(ClienteServicio clienteServicio) {
        this.clienteServicio = clienteServicio;
    }

    @GetMapping
    public List<ClienteRespuesta> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean activo) {
        return clienteServicio.listar(buscar, activo);
    }

    @GetMapping("/{id}")
    public ClienteRespuesta obtener(@PathVariable Integer id) {
        return clienteServicio.obtener(id);
    }

    @PostMapping
    public ClienteRespuesta crear(
            @Valid @RequestBody ClientePeticion peticion, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return clienteServicio.crear(peticion, principal.getUsuario());
    }

    @PutMapping("/{id}")
    public ClienteRespuesta actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ClientePeticion peticion,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return clienteServicio.actualizar(id, peticion, principal.getUsuario());
    }

    @PatchMapping("/{id}/activar")
    public ClienteRespuesta activar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return clienteServicio.activar(id, principal.getUsuario());
    }

    @PatchMapping("/{id}/desactivar")
    public ClienteRespuesta desactivar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return clienteServicio.desactivar(id, principal.getUsuario());
    }
}
