package com.minierp.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaRespuesta(
        Integer id,
        Integer numeroFactura,
        Integer clienteId,
        String clienteNombre,
        String clienteNit,
        String usuarioUsername,
        LocalDateTime fecha,
        BigDecimal subtotal,
        BigDecimal iva,
        BigDecimal total) {
}
