package com.minierp.backend.seguridad;

import tools.jackson.databind.ObjectMapper;
import com.minierp.backend.dto.ErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Se dispara cuando un usuario autenticado intenta un recurso que su rol no
// tiene permitido (403). Igual que JwtAutenticacionEntryPoint, ocurre en la
// cadena de filtros, antes del controlador.
@Component
public class JwtAccesoDenegadoManejador implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccesoDenegadoManejador(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        ErrorRespuesta error = ErrorRespuesta.de(403, "Acceso denegado", "No tiene permisos para realizar esta accion");
        objectMapper.writeValue(response.getWriter(), error);
    }
}
