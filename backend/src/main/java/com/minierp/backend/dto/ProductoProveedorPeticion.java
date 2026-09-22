package com.minierp.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductoProveedorPeticion(

        @NotNull(message = "El producto es obligatorio")
        Integer productoId,

        @NotNull(message = "El costo de referencia es obligatorio")
        @Positive(message = "El costo de referencia debe ser mayor a 0")
        BigDecimal costoReferencia) {
}
