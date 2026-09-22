package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Lote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoteRepositorio extends JpaRepository<Lote, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT l FROM Lote l
            WHERE l.producto.id = :productoId
              AND l.cantidadDisponible > 0
            ORDER BY l.fechaIngreso ASC, l.id ASC
            """)
    List<Lote> buscarConExistenciaParaActualizar(@Param("productoId") Integer productoId);

    @Query("SELECT COALESCE(SUM(l.cantidadDisponible), 0) FROM Lote l WHERE l.producto.id = :productoId")
    long sumarExistenciaPorProducto(@Param("productoId") Integer productoId);
}
