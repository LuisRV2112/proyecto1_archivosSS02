package com.minierp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoPeticion(

        @NotBlank(message = "El codigo es obligatorio")
        @Size(max = 20, message = "El codigo no puede superar 20 caracteres")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 300, message = "La descripcion no puede superar 300 caracteres")
        String descripcion,

        @NotNull(message = "La categoria es obligatoria")
        Integer categoriaId,

        @NotNull(message = "El precio de venta es obligatorio")
        @Positive(message = "El precio de venta debe ser mayor a 0")
        BigDecimal precioVenta,

        @NotNull(message = "El stock minimo es obligatorio")
        @PositiveOrZero(message = "El stock minimo no puede ser negativo")
        Integer stockMinimo) {
}
