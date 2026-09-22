package com.minierp.backend.dto;

import java.math.BigDecimal;

public record ExistenciaRespuesta(
        Integer productoId,
        String codigo,
        String nombre,
        String categoria,
        BigDecimal precioVenta,
        Integer stockMinimo,
        boolean activo,
        long existencia,
        BigDecimal valorInventario) {
}
