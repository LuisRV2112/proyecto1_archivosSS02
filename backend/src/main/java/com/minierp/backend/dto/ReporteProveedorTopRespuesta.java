package com.minierp.backend.dto;

import java.math.BigDecimal;

public record ReporteProveedorTopRespuesta(
        Integer proveedorId, String nombre, String nit, BigDecimal montoAcumulado, long cantidadCompras) {
}
