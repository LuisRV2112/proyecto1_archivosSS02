package com.minierp.backend.dto;

import java.math.BigDecimal;

public record ReporteProductoIngresoRespuesta(
        Integer productoId, String codigo, String nombre, BigDecimal ingresoTotal, long unidadesVendidas) {
}
