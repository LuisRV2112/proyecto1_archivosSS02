package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.DetalleVentaLote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleVentaLoteRepositorio extends JpaRepository<DetalleVentaLote, Integer> {

    List<DetalleVentaLote> findByDetalleVentaId(Integer detalleVentaId);
}
