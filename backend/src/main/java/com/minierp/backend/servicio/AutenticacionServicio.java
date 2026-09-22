package com.minierp.backend.servicio;

import com.minierp.backend.dto.LoginRespuesta;
import com.minierp.backend.modelo.Usuario;
import com.minierp.backend.repositorio.UsuarioRepositorio;
import com.minierp.backend.seguridad.JwtServicio;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacionServicio {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepositorio usuarioRepositorio;
    private final JwtServicio jwtServicio;
    private final BitacoraServicio bitacoraServicio;

    public AutenticacionServicio(
            AuthenticationManager authenticationManager,
            UsuarioRepositorio usuarioRepositorio,
            JwtServicio jwtServicio,
            BitacoraServicio bitacoraServicio) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepositorio = usuarioRepositorio;
        this.jwtServicio = jwtServicio;
        this.bitacoraServicio = bitacoraServicio;
    }

    public LoginRespuesta login(String username, String password) {
        try {

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException excepcionAutenticacion) {

            bitacoraServicio.registrar(null, "AUTENTICACION", "LOGIN_FALLIDO", "usuario", null,
                    "Intento de login fallido para username '" + username + "'");
            throw excepcionAutenticacion;
        }

        Usuario usuario = usuarioRepositorio.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado pero no encontrado: " + username));

        String rol = usuario.getRol().getNombre();
        String token = jwtServicio.generar(usuario.getUsername(), rol);

        bitacoraServicio.registrar(usuario, "AUTENTICACION", "LOGIN_EXITOSO", "usuario", usuario.getId(),
                "Login exitoso");

        return new LoginRespuesta(token, usuario.getUsername(), usuario.getNombreCompleto(), rol);
    }
}
