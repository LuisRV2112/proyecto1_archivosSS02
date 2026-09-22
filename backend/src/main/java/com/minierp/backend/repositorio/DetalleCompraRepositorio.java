package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.DetalleCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleCompraRepositorio extends JpaRepository<DetalleCompra, Integer> {

    List<DetalleCompra> findByCompraId(Integer compraId);
}
