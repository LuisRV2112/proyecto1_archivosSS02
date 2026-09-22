package com.minierp.backend.dto;

public record ReporteProductoFrecuenciaRespuesta(
        Integer productoId, String codigo, String nombre, long vecesComprado, long cantidadTotalComprada) {
}
