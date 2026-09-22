package com.minierp.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginPeticion(

        @NotBlank(message = "El usuario es obligatorio")
        String username,

        @NotBlank(message = "La contrasena es obligatoria")
        String password) {
}
