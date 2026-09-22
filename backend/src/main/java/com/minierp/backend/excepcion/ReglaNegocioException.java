package com.minierp.backend.excepcion;

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
