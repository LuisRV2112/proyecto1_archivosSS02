package com.minierp.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LineaCompraPeticion(

        @NotNull(message = "El producto es obligatorio")
        Integer productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a 0")
        Integer cantidad,

        @NotNull(message = "El costo unitario es obligatorio")
        @Positive(message = "El costo unitario debe ser mayor a 0")
        BigDecimal costoUnitario) {
}
