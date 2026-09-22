package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {

    // JOIN FETCH: el rol viaja siempre junto con el usuario (login, filtro JWT,
    // /api/auth/yo). Sin esto, con spring.jpa.open-in-view=false, el proxy LAZY
    // de rol queda sin sesion en cuanto termina el repositorio y cualquier
    // usuario.getRol() posterior lanza LazyInitializationException.
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.username = :username")
    Optional<Usuario> findByUsername(@Param("username") String username);
}
