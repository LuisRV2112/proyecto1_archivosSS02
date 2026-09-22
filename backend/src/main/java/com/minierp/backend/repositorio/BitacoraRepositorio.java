package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Bitacora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// JpaSpecificationExecutor permite construir los filtros de usuario, modulo y
// rango de fechas del reporte de bitacora (Fase 6) sin escribir una consulta
// distinta por cada combinacion de filtros.
public interface BitacoraRepositorio extends JpaRepository<Bitacora, Long>, JpaSpecificationExecutor<Bitacora> {
}
