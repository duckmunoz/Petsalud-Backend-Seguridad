
package com.petsalud.backendpetsalud.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey claveSecreta;

    private static final long EXPIRACION =
            1000L * 60 * 60; // 1 hora

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                "La clave JWT debe tener al menos 32 bytes"
            );
        }

        this.claveSecreta = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Generar token JWT
    public String generarToken(UserDetails usuario) {

        long ahora = System.currentTimeMillis();

        return Jwts.builder()
                .subject(usuario.getUsername())
                .issuedAt(new Date(ahora))
                .expiration(new Date(ahora + EXPIRACION))
                .signWith(claveSecreta)
                .compact();
    }

    // Extraer el correo del token
    public String extraerEmail(String token) {
        return obtenerClaims(token).getSubject();
    }

    // Validar token
    public boolean validarToken(
            String token,
            UserDetails usuario) {

        try {
            String email = extraerEmail(token);

            return email.equals(usuario.getUsername())
                    && usuario.isEnabled()
                    && usuario.isAccountNonLocked()
                    && usuario.isAccountNonExpired();

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(claveSecreta)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
