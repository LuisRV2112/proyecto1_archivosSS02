package com.minierp.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record DetalleVentaRespuesta(
        Integer id,
        Integer productoId,
        String productoCodigo,
        String productoNombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        List<LoteConsumidoRespuesta> lotes) {
}
