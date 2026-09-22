package com.minierp.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReporteResumenPeriodoRespuesta(LocalDateTime periodo, long cantidadVentas, BigDecimal montoTotal) {
}
