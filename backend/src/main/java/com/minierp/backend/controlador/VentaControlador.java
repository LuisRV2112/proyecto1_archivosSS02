package com.minierp.backend.controlador;

import com.minierp.backend.dto.VentaDetalladaRespuesta;
import com.minierp.backend.dto.VentaPeticion;
import com.minierp.backend.dto.VentaRespuesta;
import com.minierp.backend.seguridad.UsuarioPrincipal;
import com.minierp.backend.servicio.FacturaServicio;
import com.minierp.backend.servicio.VentaServicio;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/ventas")
public class VentaControlador {

    private final VentaServicio ventaServicio;
    private final FacturaServicio facturaServicio;

    public VentaControlador(VentaServicio ventaServicio, FacturaServicio facturaServicio) {
        this.ventaServicio = ventaServicio;
        this.facturaServicio = facturaServicio;
    }

    @PostMapping
    public VentaDetalladaRespuesta registrar(
            @Valid @RequestBody VentaPeticion peticion, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ventaServicio.registrar(peticion, principal.getUsuario());
    }

    @GetMapping
    public List<VentaRespuesta> listar(
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ventaServicio.listar(clienteId, desde, hasta);
    }

    @GetMapping("/{id}")
    public VentaDetalladaRespuesta obtener(@PathVariable Integer id) {
        return ventaServicio.obtener(id);
    }

    @GetMapping("/{id}/factura")
    public ResponseEntity<byte[]> factura(@PathVariable Integer id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        byte[] pdf = facturaServicio.generar(id, principal.getUsuario());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"factura-" + id + ".pdf\"")
                .body(pdf);
    }
}
