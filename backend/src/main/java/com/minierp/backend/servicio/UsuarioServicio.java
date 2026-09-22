package com.minierp.backend.servicio;

import com.minierp.backend.dto.RolRespuesta;
import com.minierp.backend.dto.UsuarioActualizacionPeticion;
import com.minierp.backend.dto.UsuarioPeticion;
import com.minierp.backend.dto.UsuarioRespuesta;
import com.minierp.backend.excepcion.RecursoDuplicadoException;
import com.minierp.backend.excepcion.RecursoNoEncontradoException;
import com.minierp.backend.modelo.Rol;
import com.minierp.backend.modelo.Usuario;
import com.minierp.backend.repositorio.RolRepositorio;
import com.minierp.backend.repositorio.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final BitacoraServicio bitacoraServicio;

    public UsuarioServicio(
            UsuarioRepositorio usuarioRepositorio,
            RolRepositorio rolRepositorio,
            PasswordEncoder passwordEncoder,
            BitacoraServicio bitacoraServicio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.bitacoraServicio = bitacoraServicio;
    }

    @Transactional(readOnly = true)
    public List<UsuarioRespuesta> listar() {
        return usuarioRepositorio.findAll().stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public List<RolRespuesta> listarRoles() {
        return rolRepositorio.findAll().stream().map(r -> new RolRespuesta(r.getId(), r.getNombre())).toList();
    }

    @Transactional
    public UsuarioRespuesta crear(UsuarioPeticion peticion, Usuario actor) {
        if (usuarioRepositorio.findByUsername(peticion.username()).isPresent()) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el username '" + peticion.username() + "'");
        }
        Rol rol = obtenerRol(peticion.rolId());

        Usuario usuario = new Usuario();
        usuario.setUsername(peticion.username());
        // BCrypt, nunca texto plano (CLAUDE.md, seccion 4)
        usuario.setPasswordHash(passwordEncoder.encode(peticion.password()));
        usuario.setNombreCompleto(peticion.nombreCompleto());
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setCreadoEn(LocalDateTime.now());
        usuarioRepositorio.save(usuario);

        bitacoraServicio.registrar(actor, "USUARIOS", "CREACION", "usuario", usuario.getId(),
                "Usuario creado: " + usuario.getUsername());
        return aRespuesta(usuario);
    }

    @Transactional
    public UsuarioRespuesta actualizar(Integer id, UsuarioActualizacionPeticion peticion, Usuario actor) {
        Usuario usuario = obtenerEntidad(id);
        Rol rol = obtenerRol(peticion.rolId());

        usuario.setNombreCompleto(peticion.nombreCompleto());
        usuario.setRol(rol);

        bitacoraServicio.registrar(actor, "USUARIOS", "ACTUALIZACION", "usuario", usuario.getId(),
                "Usuario actualizado: " + usuario.getUsername());
        return aRespuesta(usuario);
    }

    @Transactional
    public UsuarioRespuesta activar(Integer id, Usuario actor) {
        return cambiarEstado(id, true, actor);
    }

    @Transactional
    public UsuarioRespuesta desactivar(Integer id, Usuario actor) {
        return cambiarEstado(id, false, actor);
    }

    private UsuarioRespuesta cambiarEstado(Integer id, boolean activo, Usuario actor) {
        // Borrado logico: nunca DELETE. Un usuario desactivado no puede
        // autenticarse (ver UsuarioPrincipal.isEnabled, Fase 2) pero su
        // historial en compras/ventas/bitacora se conserva.
        Usuario usuario = obtenerEntidad(id);
        usuario.setActivo(activo);

        bitacoraServicio.registrar(actor, "USUARIOS", activo ? "ACTIVACION" : "DESACTIVACION",
                "usuario", usuario.getId(),
                "Usuario " + (activo ? "activado" : "desactivado") + ": " + usuario.getUsername());
        return aRespuesta(usuario);
    }

    private Rol obtenerRol(Short rolId) {
        return rolRepositorio.findById(rolId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + rolId));
    }

    private Usuario obtenerEntidad(Integer id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
    }

    private UsuarioRespuesta aRespuesta(Usuario usuario) {
        return new UsuarioRespuesta(
                usuario.getId(), usuario.getUsername(), usuario.getNombreCompleto(),
                usuario.getRol().getNombre(), usuario.isActivo());
    }
}
