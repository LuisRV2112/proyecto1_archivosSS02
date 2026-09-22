package com.minierp.backend.dto;

import java.math.BigDecimal;

public record ProductoRespuesta(
        Integer id,
        String codigo,
        String nombre,
        String descripcion,
        Integer categoriaId,
        String categoriaNombre,
        BigDecimal precioVenta,
        Integer stockMinimo,
        boolean activo,
        long existencia) {
}
