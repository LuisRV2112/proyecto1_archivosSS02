package com.minierp.backend.dto;

public record ProveedorRespuesta(
        Integer id,
        String nit,
        String nombre,
        String telefono,
        String correo,
        String direccion,
        boolean activo) {
}
