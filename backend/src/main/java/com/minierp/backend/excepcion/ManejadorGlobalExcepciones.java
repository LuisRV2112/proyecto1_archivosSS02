package com.minierp.backend.excepcion;

import com.minierp.backend.dto.ErrorRespuesta;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// Maneja las excepciones que ocurren DENTRO de un controlador (validacion,
// login fallido, errores inesperados). Las que ocurren en la cadena de
// filtros de Spring Security (token ausente/invalido, rol sin permiso) no
// llegan aqui: las maneja JwtAutenticacionEntryPoint y
// JwtAccesoDenegadoManejador, con el mismo formato de error.
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRespuesta manejarValidacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ErrorRespuesta.de(400, "Datos invalidos", detalle);
    }

    // Cubre BadCredentialsException (password incorrecto) y
    // DisabledException (usuario inactivo), lanzadas desde AutenticacionServicio
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorRespuesta manejarAutenticacion(AuthenticationException ex) {
        return ErrorRespuesta.de(401, "Credenciales invalidas", "Usuario o contrasena incorrectos");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorRespuesta manejarAccesoDenegado(AccessDeniedException ex) {
        return ErrorRespuesta.de(403, "Acceso denegado", "No tiene permisos para realizar esta accion");
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorRespuesta manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return ErrorRespuesta.de(404, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorRespuesta manejarDuplicado(RecursoDuplicadoException ex) {
        return ErrorRespuesta.de(409, "Recurso duplicado", ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRespuesta manejarReglaNegocio(ReglaNegocioException ex) {
        return ErrorRespuesta.de(400, ex.getTitulo(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorRespuesta manejarGeneral(Exception ex) {
        return ErrorRespuesta.de(500, "Error interno", ex.getMessage());
    }
}
