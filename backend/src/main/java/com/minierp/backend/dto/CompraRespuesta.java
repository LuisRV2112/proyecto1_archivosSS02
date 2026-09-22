package com.minierp.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompraRespuesta(
        Integer id,
        Integer proveedorId,
        String proveedorNombre,
        String usuarioUsername,
        LocalDateTime fecha,
        String numeroDocumento,
        BigDecimal total,
        String observaciones) {
}
