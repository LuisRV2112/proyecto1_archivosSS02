package com.minierp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioActualizacionPeticion(

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 100)
        String nombreCompleto,

        @NotNull(message = "El rol es obligatorio")
        Short rolId) {
}
