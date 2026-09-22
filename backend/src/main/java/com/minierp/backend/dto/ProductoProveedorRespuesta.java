package com.minierp.backend.dto;

import java.math.BigDecimal;

public record ProductoProveedorRespuesta(
        Integer productoId,
        String productoCodigo,
        String productoNombre,
        BigDecimal costoReferencia) {
}
