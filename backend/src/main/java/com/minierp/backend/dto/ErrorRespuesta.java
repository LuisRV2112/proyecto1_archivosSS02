package com.minierp.backend.dto;

import java.time.LocalDateTime;

// Formato de error consistente para toda la API (CLAUDE.md, seccion 8)
public record ErrorRespuesta(int estado, String error, String detalle, LocalDateTime fecha) {

    public static ErrorRespuesta de(int estado, String error, String detalle) {
        return new ErrorRespuesta(estado, error, detalle, LocalDateTime.now());
    }
}
