package com.minierp.backend.config;

import com.minierp.backend.seguridad.JwtAccesoDenegadoManejador;
import com.minierp.backend.seguridad.JwtAutenticacionEntryPoint;
import com.minierp.backend.seguridad.JwtAutenticacionFiltro;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private static final String ADMINISTRACION = "ADMINISTRACION";
    private static final String COMPRAS = "COMPRAS";
    private static final String INVENTARIO = "INVENTARIO";
    private static final String VENTAS = "VENTAS";

    private final JwtAutenticacionFiltro jwtAutenticacionFiltro;
    private final JwtAutenticacionEntryPoint jwtAutenticacionEntryPoint;
    private final JwtAccesoDenegadoManejador jwtAccesoDenegadoManejador;

    public SecurityConfig(
            JwtAutenticacionFiltro jwtAutenticacionFiltro,
            JwtAutenticacionEntryPoint jwtAutenticacionEntryPoint,
            JwtAccesoDenegadoManejador jwtAccesoDenegadoManejador) {
        this.jwtAutenticacionFiltro = jwtAutenticacionFiltro;
        this.jwtAutenticacionEntryPoint = jwtAutenticacionEntryPoint;
        this.jwtAccesoDenegadoManejador = jwtAccesoDenegadoManejador;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(manejo -> manejo
                        .authenticationEntryPoint(jwtAutenticacionEntryPoint)
                        .accessDeniedHandler(jwtAccesoDenegadoManejador))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/yo").authenticated()

                        .requestMatchers("/api/usuarios/**").hasRole(ADMINISTRACION)
                        .requestMatchers("/api/bitacora/**").hasRole(ADMINISTRACION)

                        .requestMatchers(HttpMethod.GET, "/api/categorias/**")
                        .hasAnyRole(ADMINISTRACION, COMPRAS, INVENTARIO, VENTAS)
                        .requestMatchers("/api/categorias/**").hasAnyRole(ADMINISTRACION, INVENTARIO)

                        .requestMatchers(HttpMethod.GET, "/api/productos/**")
                        .hasAnyRole(ADMINISTRACION, COMPRAS, INVENTARIO, VENTAS)
                        .requestMatchers("/api/productos/**").hasAnyRole(ADMINISTRACION, INVENTARIO)

                        .requestMatchers("/api/proveedores/**").hasAnyRole(ADMINISTRACION, COMPRAS)
                        .requestMatchers("/api/compras/**").hasAnyRole(ADMINISTRACION, COMPRAS)

                        .requestMatchers("/api/inventario/alertas").hasAnyRole(ADMINISTRACION, COMPRAS, INVENTARIO)
                        .requestMatchers("/api/inventario/**")
                        .hasAnyRole(ADMINISTRACION, COMPRAS, INVENTARIO, VENTAS)

                        .requestMatchers("/api/clientes/**").hasAnyRole(ADMINISTRACION, VENTAS)
                        .requestMatchers("/api/ventas/**").hasAnyRole(ADMINISTRACION, VENTAS)

                        .requestMatchers("/api/reportes/productos/**", "/api/reportes/inventario/**")
                        .hasAnyRole(ADMINISTRACION, INVENTARIO)
                        .requestMatchers("/api/reportes/compras/**", "/api/reportes/proveedores/**")
                        .hasAnyRole(ADMINISTRACION, COMPRAS)
                        .requestMatchers("/api/reportes/ventas/**", "/api/reportes/clientes/**")
                        .hasAnyRole(ADMINISTRACION, VENTAS)

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAutenticacionFiltro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(List.of("http://localhost:4200"));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));
        configuracion.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }
}
