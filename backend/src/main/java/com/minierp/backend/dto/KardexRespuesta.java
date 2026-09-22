package com.minierp.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record KardexRespuesta(
        Integer id,
        LocalDateTime fecha,
        String tipo,
        String origen,
        Integer referenciaId,
        Integer loteId,
        Integer cantidad,
        BigDecimal costoUnitario,
        Integer existenciaResultante,
        String usuarioUsername) {
}
