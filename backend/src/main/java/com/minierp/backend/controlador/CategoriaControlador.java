package com.minierp.backend.controlador;

import com.minierp.backend.dto.CategoriaPeticion;
import com.minierp.backend.dto.CategoriaRespuesta;
import com.minierp.backend.seguridad.UsuarioPrincipal;
import com.minierp.backend.servicio.CategoriaServicio;
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
@RequestMapping("/api/categorias")
public class CategoriaControlador {

    private final CategoriaServicio categoriaServicio;

    public CategoriaControlador(CategoriaServicio categoriaServicio) {
        this.categoriaServicio = categoriaServicio;
    }

    @GetMapping
    public List<CategoriaRespuesta> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean activo) {
        return categoriaServicio.listar(buscar, activo);
    }

    @GetMapping("/{id}")
    public CategoriaRespuesta obtener(@PathVariable Integer id) {
        return categoriaServicio.obtener(id);
    }

    @PostMapping
    public CategoriaRespuesta crear(
            @Valid @RequestBody CategoriaPeticion peticion, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return categoriaServicio.crear(peticion, principal.getUsuario());
    }

    @PutMapping("/{id}")
    public CategoriaRespuesta actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaPeticion peticion,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return categoriaServicio.actualizar(id, peticion, principal.getUsuario());
    }

    @PatchMapping("/{id}/activar")
    public CategoriaRespuesta activar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return categoriaServicio.activar(id, principal.getUsuario());
    }

    @PatchMapping("/{id}/desactivar")
    public CategoriaRespuesta desactivar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return categoriaServicio.desactivar(id, principal.getUsuario());
    }
}
