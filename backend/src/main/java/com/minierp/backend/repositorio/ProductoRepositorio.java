package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoRepositorio extends JpaRepository<Producto, Integer> {

    // Para el mensaje claro de codigo de producto duplicado (Fase 3)
    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Integer id);

    // CAST(:buscar AS string): ver el comentario en CategoriaRepositorio.buscar
    @Query("""
            SELECT p FROM Producto p
            WHERE (:activo IS NULL OR p.activo = :activo)
              AND (:buscar IS NULL
                   OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
                   OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%')))
            ORDER BY p.nombre
            """)
    List<Producto> buscar(@Param("buscar") String buscar, @Param("activo") Boolean activo);
}
