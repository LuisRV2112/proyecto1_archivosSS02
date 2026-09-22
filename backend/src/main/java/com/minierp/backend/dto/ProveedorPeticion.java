package com.minierp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorPeticion(

        @NotBlank(message = "El NIT es obligatorio")
        @Size(max = 15, message = "El NIT no puede superar 15 caracteres")
        String nit,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 20)
        String telefono,

        @Size(max = 100)
        String correo,

        @Size(max = 200)
        String direccion) {
}
