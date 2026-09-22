package com.minierp.backend.controlador;

import com.minierp.backend.dto.CompraDetalladaRespuesta;
import com.minierp.backend.dto.CompraPeticion;
import com.minierp.backend.dto.CompraRespuesta;
import com.minierp.backend.seguridad.UsuarioPrincipal;
import com.minierp.backend.servicio.CompraServicio;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/compras")
public class CompraControlador {

    private final CompraServicio compraServicio;

    public CompraControlador(CompraServicio compraServicio) {
        this.compraServicio = compraServicio;
    }

    @PostMapping
    public CompraDetalladaRespuesta registrar(
            @Valid @RequestBody CompraPeticion peticion, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return compraServicio.registrar(peticion, principal.getUsuario());
    }

    @GetMapping
    public List<CompraRespuesta> listar(
            @RequestParam(required = false) Integer proveedorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return compraServicio.listar(proveedorId, desde, hasta);
    }

    @GetMapping("/{id}")
    public CompraDetalladaRespuesta obtener(@PathVariable Integer id) {
        return compraServicio.obtener(id);
    }
}
