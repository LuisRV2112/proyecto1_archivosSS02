package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CompraRepositorio extends JpaRepository<Compra, Integer>, JpaSpecificationExecutor<Compra> {
}
