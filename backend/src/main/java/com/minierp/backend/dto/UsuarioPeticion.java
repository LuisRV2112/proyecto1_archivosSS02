package com.minierp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioPeticion(

        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 40, message = "El usuario no puede superar 40 caracteres")
        String username,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
        String password,

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 100)
        String nombreCompleto,

        @NotNull(message = "El rol es obligatorio")
        Short rolId) {
}
