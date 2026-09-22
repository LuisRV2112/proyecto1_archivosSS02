package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepositorio extends JpaRepository<Rol, Short> {
}
