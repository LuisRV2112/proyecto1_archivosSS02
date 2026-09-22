package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VentaRepositorio extends JpaRepository<Venta, Integer>, JpaSpecificationExecutor<Venta> {
}
