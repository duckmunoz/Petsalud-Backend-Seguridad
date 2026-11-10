
package com.petsalud.backendpetsalud.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtFilter,
            UsuarioDetailsService usuarioDetailsService) {

        this.jwtFilter = jwtFilter;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    
@Bean
public SecurityFilterChain securityFilterChain(
        HttpSecurity http) throws Exception {

    http
        // API REST con JWT
        .csrf(csrf -> csrf.disable())

        // Sin sesiones de servidor
        .sessionManagement(session -> session
            .sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
            )
        )

        // Desactivar autenticacion tradicional
        .httpBasic(basic -> basic.disable())
        .formLogin(form -> form.disable())

        // Reglas de autorizacion
        .authorizeHttpRequests(auth -> auth

            // Registro e inicio de sesion publicos
            .requestMatchers(
                "/api/usuarios/registro",
                "/api/login"
            ).permitAll()

            // Listado de usuarios
            .requestMatchers(
                org.springframework.http.HttpMethod.GET,
                "/api/usuarios"
            ).hasAnyAuthority("GERENTE", "ADMIN")

            // Eliminacion de usuarios
            .requestMatchers(
                org.springframework.http.HttpMethod.DELETE,
                "/api/usuarios/*"
            ).hasAnyAuthority("GERENTE", "ADMIN")

            // Rutas administrativas
            .requestMatchers("/api/admin/**")
            .hasAnyAuthority("GERENTE", "ADMIN")

            // Rutas de empleados
            .requestMatchers("/api/empleado/**")
            .hasAnyAuthority(
                "EMPLEADO", "GERENTE", "ADMIN"
            )

            // Rutas veterinarias existentes
            .requestMatchers("/api/veterinario/**")
            .hasAnyAuthority(
                "VETERINARIO", "GERENTE", "ADMIN"
            )

            // Otros endpoints requieren autenticacion
            .anyRequest().authenticated()
        )

        // Servicio de usuarios
        .userDetailsService(usuarioDetailsService)

        // IMPORTANTE: registrar el filtro JWT
        .addFilterBefore(
            jwtFilter,
            UsernamePasswordAuthenticationFilter.class
        );

    return http.build();
}


@Bean
public FilterRegistrationBean<JwtAuthenticationFilter>
        jwtFilterRegistration(
                JwtAuthenticationFilter jwtFilter) {

    FilterRegistrationBean<JwtAuthenticationFilter> registration =
            new FilterRegistrationBean<>(jwtFilter);

    registration.setEnabled(false);

    return registration;
}


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }
}
