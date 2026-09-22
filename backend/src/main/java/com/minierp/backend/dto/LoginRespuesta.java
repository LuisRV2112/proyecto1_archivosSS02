package com.minierp.backend.dto;

public record LoginRespuesta(String token, String username, String nombreCompleto, String rol) {
}
