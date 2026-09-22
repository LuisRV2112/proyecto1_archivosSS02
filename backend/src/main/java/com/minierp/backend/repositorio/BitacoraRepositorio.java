package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Bitacora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BitacoraRepositorio extends JpaRepository<Bitacora, Long>, JpaSpecificationExecutor<Bitacora> {
}
