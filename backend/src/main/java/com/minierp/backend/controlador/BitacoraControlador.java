package com.minierp.backend.controlador;

import com.minierp.backend.dto.BitacoraRespuesta;
import com.minierp.backend.servicio.BitacoraServicio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

// Solo ADMINISTRACION (SecurityConfig, /api/bitacora/**)
@RestController
@RequestMapping("/api/bitacora")
public class BitacoraControlador {

    private final BitacoraServicio bitacoraServicio;

    public BitacoraControlador(BitacoraServicio bitacoraServicio) {
        this.bitacoraServicio = bitacoraServicio;
    }

    @GetMapping
    public List<BitacoraRespuesta> consultar(
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return bitacoraServicio.consultar(usuarioId, modulo, desde, hasta);
    }
}
