package com.minierp.backend.seguridad;

import tools.jackson.databind.ObjectMapper;
import com.minierp.backend.dto.ErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Se dispara cuando una peticion sin token (o con uno invalido/expirado) llega
// a un recurso protegido. Ocurre DENTRO de la cadena de filtros de Spring
// Security, antes de que el request llegue a un controlador, por lo que
// ManejadorGlobalExcepciones (@RestControllerAdvice) nunca la veria: hay que
// devolver el JSON de error aqui mismo.
@Component
public class JwtAutenticacionEntryPoint implements AuthenticationEntryPoint {

    // tools.jackson (Jackson 3), no com.fasterxml.jackson: es el ObjectMapper
    // que Spring Boot 4 autoconfigura y expone como bean
    private final ObjectMapper objectMapper;

    public JwtAutenticacionEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ErrorRespuesta error = ErrorRespuesta.de(401, "No autenticado", "Se requiere un token valido");
        objectMapper.writeValue(response.getWriter(), error);
    }
}
