package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleVentaRepositorio extends JpaRepository<DetalleVenta, Integer> {

    List<DetalleVenta> findByVentaId(Integer ventaId);
}
