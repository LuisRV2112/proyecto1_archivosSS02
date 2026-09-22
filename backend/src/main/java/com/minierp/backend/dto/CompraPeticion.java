package com.minierp.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record CompraPeticion(

        @NotNull(message = "El proveedor es obligatorio")
        Integer proveedorId,

        @NotNull(message = "La fecha es obligatoria")
        LocalDateTime fecha,

        @Size(max = 30, message = "El numero de documento no puede superar 30 caracteres")
        String numeroDocumento,

        @Size(max = 300, message = "Las observaciones no pueden superar 300 caracteres")
        String observaciones,

        @NotEmpty(message = "La compra debe tener al menos una linea")
        @Valid
        List<LineaCompraPeticion> lineas) {
}
