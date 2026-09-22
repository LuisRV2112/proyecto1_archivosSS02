package com.minierp.backend.excepcion;

// Violaciones de reglas de negocio que un DTO no puede expresar con
// anotaciones de validacion: "el proveedor no surte este producto" (Fase 4),
// "existencia insuficiente" o "producto/cliente inactivo" (Fase 5). El titulo
// va en el campo "error" de la respuesta, el detalle en "detalle" (CLAUDE.md,
// seccion 8: { "estado": 400, "error": "Existencia insuficiente", ... }).
public class ReglaNegocioException extends RuntimeException {

    private final String titulo;

    public ReglaNegocioException(String titulo, String detalle) {
        super(detalle);
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }
}
