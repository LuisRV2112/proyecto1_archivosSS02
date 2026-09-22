package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepositorio extends JpaRepository<Venta, Integer> {

    // CAST(...): ver el comentario en CompraRepositorio.buscar
    @Query("""
            SELECT v FROM Venta v
            WHERE (:clienteId IS NULL OR v.cliente.id = :clienteId)
              AND (CAST(:desde AS timestamp) IS NULL OR v.fecha >= CAST(:desde AS timestamp))
              AND (CAST(:hasta AS timestamp) IS NULL OR v.fecha <= CAST(:hasta AS timestamp))
            ORDER BY v.fecha DESC, v.id DESC
            """)
    List<Venta> buscar(
            @Param("clienteId") Integer clienteId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
