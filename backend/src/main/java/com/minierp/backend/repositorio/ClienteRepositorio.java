package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteRepositorio extends JpaRepository<Cliente, Integer> {

    boolean existsByNit(String nit);

    boolean existsByNitAndIdNot(String nit, Integer id);

    @Query("""
            SELECT c FROM Cliente c
            WHERE (:activo IS NULL OR c.activo = :activo)
              AND (:buscar IS NULL
                   OR LOWER(c.nit) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
                   OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%')))
            ORDER BY c.nombre
            """)
    List<Cliente> buscar(@Param("buscar") String buscar, @Param("activo") Boolean activo);
}
