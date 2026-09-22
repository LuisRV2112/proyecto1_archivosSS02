package com.minierp.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// De que lote salio parte (o todo) de una linea de venta: la trazabilidad PEPS
// que pide la Fase 5 ("ver el detalle incluyendo de que lotes salio cada linea")
public record LoteConsumidoRespuesta(
        Integer loteId,
        LocalDateTime fechaIngresoLote,
        Integer cantidad,
        BigDecimal costoUnitario) {
}
