package com.minierp.backend.controlador;

import com.minierp.backend.dto.ProductoProveedorPeticion;
import com.minierp.backend.dto.ProductoProveedorRespuesta;
import com.minierp.backend.dto.ProveedorPeticion;
import com.minierp.backend.dto.ProveedorRespuesta;
import com.minierp.backend.seguridad.UsuarioPrincipal;
import com.minierp.backend.servicio.ProveedorServicio;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/proveedores")
public class ProveedorControlador {

    private final ProveedorServicio proveedorServicio;

    public ProveedorControlador(ProveedorServicio proveedorServicio) {
        this.proveedorServicio = proveedorServicio;
    }

    @GetMapping
    public List<ProveedorRespuesta> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean activo) {
        return proveedorServicio.listar(buscar, activo);
    }

    @GetMapping("/{id}")
    public ProveedorRespuesta obtener(@PathVariable Integer id) {
        return proveedorServicio.obtener(id);
    }

    @PostMapping
    public ProveedorRespuesta crear(
            @Valid @RequestBody ProveedorPeticion peticion, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return proveedorServicio.crear(peticion, principal.getUsuario());
    }

    @PutMapping("/{id}")
    public ProveedorRespuesta actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ProveedorPeticion peticion,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return proveedorServicio.actualizar(id, peticion, principal.getUsuario());
    }

    @PatchMapping("/{id}/activar")
    public ProveedorRespuesta activar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return proveedorServicio.activar(id, principal.getUsuario());
    }

    @PatchMapping("/{id}/desactivar")
    public ProveedorRespuesta desactivar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return proveedorServicio.desactivar(id, principal.getUsuario());
    }

    @GetMapping("/{id}/productos")
    public List<ProductoProveedorRespuesta> listarProductos(@PathVariable Integer id) {
        return proveedorServicio.listarProductos(id);
    }

    @PostMapping("/{id}/productos")
    public ProductoProveedorRespuesta asociarProducto(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoProveedorPeticion peticion,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return proveedorServicio.asociarProducto(id, peticion, principal.getUsuario());
    }

    @DeleteMapping("/{id}/productos/{productoId}")
    public void desasociarProducto(
            @PathVariable Integer id, @PathVariable Integer productoId, @AuthenticationPrincipal UsuarioPrincipal principal) {
        proveedorServicio.desasociarProducto(id, productoId, principal.getUsuario());
    }
}
