package com.minierp.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoteConsumidoRespuesta(
        Integer loteId,
        LocalDateTime fechaIngresoLote,
        Integer cantidad,
        BigDecimal costoUnitario) {
}
