package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CompraRepositorio extends JpaRepository<Compra, Integer> {

    // CAST(:desde/:hasta AS timestamp) en CADA aparicion, incluida la del
    // "IS NULL": a diferencia de un parametro de texto (donde PostgreSQL
    // asume "unknown" ~ text por defecto), con timestamp cada placeholder sin
    // tipo explicito falla con "could not determine data type of parameter"
    @Query("""
            SELECT c FROM Compra c
            WHERE (:proveedorId IS NULL OR c.proveedor.id = :proveedorId)
              AND (CAST(:desde AS timestamp) IS NULL OR c.fecha >= CAST(:desde AS timestamp))
              AND (CAST(:hasta AS timestamp) IS NULL OR c.fecha <= CAST(:hasta AS timestamp))
            ORDER BY c.fecha DESC, c.id DESC
            """)
    List<Compra> buscar(
            @Param("proveedorId") Integer proveedorId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
