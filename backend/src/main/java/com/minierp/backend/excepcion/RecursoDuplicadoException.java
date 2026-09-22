package com.minierp.backend.excepcion;

// Se usa para los mensajes claros de duplicados que pide la Fase 3 (codigo de
// producto, NIT de proveedor o cliente, nombre de categoria)
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
