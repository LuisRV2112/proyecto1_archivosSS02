package com.minierp.backend.dto;

import java.time.LocalDateTime;

public record BitacoraRespuesta(
        Long id,
        String usuarioUsername,
        String modulo,
        String accion,
        String entidad,
        Integer entidadId,
        String detalle,
        LocalDateTime fecha) {
}
