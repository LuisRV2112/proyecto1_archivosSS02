package com.minierp.backend.dto;

import java.math.BigDecimal;

public record ReporteClienteTopRespuesta(
        Integer clienteId, String nombre, String nit, BigDecimal montoAcumulado, long cantidadCompras) {
}
