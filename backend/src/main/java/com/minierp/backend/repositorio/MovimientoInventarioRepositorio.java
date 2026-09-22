package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepositorio extends JpaRepository<MovimientoInventario, Integer> {

    // Kardex de un producto, del movimiento mas antiguo al mas reciente
    List<MovimientoInventario> findByProductoIdOrderByFechaAscIdAsc(Integer productoId);
}
