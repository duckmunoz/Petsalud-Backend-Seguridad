
package com.petsalud.backendpetsalud.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");
                
System.out.println(
    "[JWT] Ruta: " + request.getRequestURI()
);

System.out.println(
    "[JWT] Authorization presente: "
    + (authorization != null)
);

System.out.println(
    "[JWT] Formato Bearer correcto: "
    + (authorization != null
        && authorization.startsWith("Bearer "))
);


        if (authorization != null
                && authorization.startsWith("Bearer ")) {

            String token = authorization.substring(7);

            try {
                String email = jwtUtil.extraerEmail(token);

                if (email != null
                        && SecurityContextHolder.getContext()
                                .getAuthentication() == null) {

                    UserDetails usuario =
                            usuarioDetailsService
                                    .loadUserByUsername(email);

System.out.println(
    "[JWT] Usuario encontrado: "
    + (usuario != null)
);

System.out.println(
    "[JWT] Token valido: "
    + jwtUtil.validarToken(token, usuario)
);

                    if (jwtUtil.validarToken(token, usuario)) {

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        usuario,
                                        null,
                                        usuario.getAuthorities()
                                );

                        auth.setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request)
                        );

                        SecurityContextHolder.getContext()
                                .setAuthentication(auth);
                    }
                }

            
} catch (Exception e) {
    SecurityContextHolder.clearContext();

    System.err.println(
        "[JWT] Error de autenticacion: "
        + e.getClass().getSimpleName()
        + " - " + e.getMessage()
    );
}

        }

        filterChain.doFilter(request, response);
    }
}
