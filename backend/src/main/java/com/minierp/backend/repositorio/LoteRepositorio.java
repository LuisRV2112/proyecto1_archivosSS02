package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Lote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoteRepositorio extends JpaRepository<Lote, Integer> {

    // Metodo PEPS: lotes de un producto con existencia, del mas antiguo al mas
    // reciente (fechaIngreso y luego id, para desempatar compras del mismo
    // instante). PESSIMISTIC_WRITE evita que dos ventas concurrentes lean el
    // mismo lote y lo descuenten dos veces (el clasico problema de carrera al
    // vender el ultimo producto disponible visto en clase): la fila queda
    // bloqueada (SELECT ... FOR UPDATE) hasta que la transaccion que vende
    // termine, y la siguiente venta ve ya el saldo actualizado.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT l FROM Lote l
            WHERE l.producto.id = :productoId
              AND l.cantidadDisponible > 0
            ORDER BY l.fechaIngreso ASC, l.id ASC
            """)
    List<Lote> buscarConExistenciaParaActualizar(@Param("productoId") Integer productoId);

    // Existencia total del producto en este momento (suma de todos sus lotes),
    // para calcular movimiento_inventario.existencia_resultante al registrar
    // una compra o una venta.
    @Query("SELECT COALESCE(SUM(l.cantidadDisponible), 0) FROM Lote l WHERE l.producto.id = :productoId")
    long sumarExistenciaPorProducto(@Param("productoId") Integer productoId);
}
