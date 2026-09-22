package com.minierp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// A diferencia de UsuarioPeticion, no permite cambiar username ni password:
// esta pensado solo para editar nombre y rol desde el modulo de administracion
public record UsuarioActualizacionPeticion(

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 100)
        String nombreCompleto,

        @NotNull(message = "El rol es obligatorio")
        Short rolId) {
}
