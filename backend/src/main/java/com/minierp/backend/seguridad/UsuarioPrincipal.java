package com.minierp.backend.seguridad;

import com.minierp.backend.modelo.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Envuelve la entidad Usuario para que Spring Security pueda autenticarla y
// autorizarla, sin exponer la entidad JPA directamente al resto del sistema.
public class UsuarioPrincipal implements UserDetails {

    private final Usuario usuario;

    public UsuarioPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security espera el prefijo "ROLE_" para que hasRole(...)
        // funcione; en la base el rol se guarda sin el (ADMINISTRACION, etc.)
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()));
    }

    @Override
    public String getPassword() {
        return usuario.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Un usuario desactivado no puede autenticarse aunque la contrasena sea
    // correcta: Spring Security revisa este metodo automaticamente durante la
    // autenticacion (DisabledException) y tambien en cada peticion, en
    // JwtAutenticacionFiltro.
    @Override
    public boolean isEnabled() {
        return usuario.isActivo();
    }
}
