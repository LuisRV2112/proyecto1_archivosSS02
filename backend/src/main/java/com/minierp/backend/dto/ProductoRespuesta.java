package com.minierp.backend.dto;

import java.math.BigDecimal;

// La existencia viaja siempre con el producto (desde v_existencia): el
// enunciado pide incluirla en el listado, y separarla obligaria al frontend a
// hacer una segunda peticion por cada producto.
public record ProductoRespuesta(
        Integer id,
        String codigo,
        String nombre,
        String descripcion,
        Integer categoriaId,
        String categoriaNombre,
        BigDecimal precioVenta,
        Integer stockMinimo,
        boolean activo,
        long existencia) {
}
