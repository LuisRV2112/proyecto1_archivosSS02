package com.minierp.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VentaPeticion(

        @NotNull(message = "El cliente es obligatorio")
        Integer clienteId,

        @NotEmpty(message = "La venta debe tener al menos una linea")
        @Valid
        List<LineaVentaPeticion> lineas) {
}
