package com.minierp.backend.dto;

import java.time.LocalDateTime;

public record ErrorRespuesta(int estado, String error, String detalle, LocalDateTime fecha) {

    public static ErrorRespuesta de(int estado, String error, String detalle) {
        return new ErrorRespuesta(estado, error, detalle, LocalDateTime.now());
    }
}
