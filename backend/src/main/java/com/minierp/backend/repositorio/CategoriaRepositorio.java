package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Integer> {

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Integer id);

    @Query("""
            SELECT c FROM Categoria c
            WHERE (:activo IS NULL OR c.activo = :activo)
              AND (:buscar IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%')))
            ORDER BY c.nombre
            """)
    List<Categoria> buscar(@Param("buscar") String buscar, @Param("activo") Boolean activo);
}
