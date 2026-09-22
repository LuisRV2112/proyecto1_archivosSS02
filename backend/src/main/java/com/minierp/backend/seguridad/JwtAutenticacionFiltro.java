package com.minierp.backend.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Lee el header "Authorization: Bearer <token>" en cada peticion. Si el token
// es valido, vuelve a cargar el usuario desde la base (no se confia solo en lo
// que dice el token) para que una desactivacion o un cambio de rol tengan
// efecto inmediato, en vez de esperar a que el token expire (hasta 8 horas).
@Component
public class JwtAutenticacionFiltro extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtServicio jwtServicio;
    private final UserDetailsService usuarioDetailsServicio;

    public JwtAutenticacionFiltro(JwtServicio jwtServicio, UserDetailsService usuarioDetailsServicio) {
        this.jwtServicio = jwtServicio;
        this.usuarioDetailsServicio = usuarioDetailsServicio;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith(PREFIJO_BEARER)) {
            String token = encabezado.substring(PREFIJO_BEARER.length());

            if (jwtServicio.esValido(token)) {
                String username = jwtServicio.extraerUsername(token);
                try {
                    UserDetails usuario = usuarioDetailsServicio.loadUserByUsername(username);
                    if (usuario.isEnabled()) {
                        UsernamePasswordAuthenticationToken autenticacion =
                                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                        autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    }
                } catch (UsernameNotFoundException e) {
                    // Token valido pero el usuario ya no existe: se deja la
                    // peticion sin autenticar, el filtro de autorizacion la
                    // rechazara mas adelante.
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
