package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProveedorRepositorio extends JpaRepository<Proveedor, Integer> {

    // Para el mensaje claro de NIT duplicado (Fase 3)
    boolean existsByNit(String nit);

    boolean existsByNitAndIdNot(String nit, Integer id);

    // CAST(:buscar AS string): ver el comentario en CategoriaRepositorio.buscar
    @Query("""
            SELECT p FROM Proveedor p
            WHERE (:activo IS NULL OR p.activo = :activo)
              AND (:buscar IS NULL
                   OR LOWER(p.nit) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
                   OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%')))
            ORDER BY p.nombre
            """)
    List<Proveedor> buscar(@Param("buscar") String buscar, @Param("activo") Boolean activo);
}
