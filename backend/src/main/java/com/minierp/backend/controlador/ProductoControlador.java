package com.minierp.backend.controlador;

import com.minierp.backend.dto.ProductoPeticion;
import com.minierp.backend.dto.ProductoRespuesta;
import com.minierp.backend.seguridad.UsuarioPrincipal;
import com.minierp.backend.servicio.ProductoServicio;
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
@RequestMapping("/api/productos")
public class ProductoControlador {

    private final ProductoServicio productoServicio;

    public ProductoControlador(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    @GetMapping
    public List<ProductoRespuesta> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean activo) {
        return productoServicio.listar(buscar, activo);
    }

    @GetMapping("/{id}")
    public ProductoRespuesta obtener(@PathVariable Integer id) {
        return productoServicio.obtener(id);
    }

    @PostMapping
    public ProductoRespuesta crear(
            @Valid @RequestBody ProductoPeticion peticion, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return productoServicio.crear(peticion, principal.getUsuario());
    }

    @PutMapping("/{id}")
    public ProductoRespuesta actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoPeticion peticion,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return productoServicio.actualizar(id, peticion, principal.getUsuario());
    }

    @PatchMapping("/{id}/activar")
    public ProductoRespuesta activar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return productoServicio.activar(id, principal.getUsuario());
    }

    @PatchMapping("/{id}/desactivar")
    public ProductoRespuesta desactivar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return productoServicio.desactivar(id, principal.getUsuario());
    }
}
