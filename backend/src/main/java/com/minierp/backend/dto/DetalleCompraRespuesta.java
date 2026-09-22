package com.minierp.backend.dto;

import java.math.BigDecimal;

public record DetalleCompraRespuesta(
        Integer id,
        Integer productoId,
        String productoCodigo,
        String productoNombre,
        Integer cantidad,
        BigDecimal costoUnitario,
        BigDecimal subtotal) {
}
