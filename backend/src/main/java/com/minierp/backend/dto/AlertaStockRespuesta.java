package com.minierp.backend.dto;

public record AlertaStockRespuesta(
        Integer productoId,
        String codigo,
        String nombre,
        String categoria,
        long existencia,
        Integer stockMinimo,
        long faltante,
        String nivel) {
}
