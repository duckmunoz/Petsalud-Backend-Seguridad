
package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Recompensa;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.service.RecompensaService;
import com.petsalud.backendpetsalud.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recompensas")
public class RecompensaController {

    @Autowired
    private RecompensaService recompensaService;

    @Autowired
    private UsuarioService usuarioService;

    private boolean esAdministrador(Authentication auth) {
        return auth != null &&
                auth.getAuthorities().stream().anyMatch(a ->
                        a.getAuthority().equals("GERENTE") ||
                        a.getAuthority().equals("ADMIN"));
    }

    private boolean puedeConsultarTodo(Authentication auth) {
        return auth != null &&
                auth.getAuthorities().stream().anyMatch(a ->
                        a.getAuthority().equals("EMPLEADO") ||
                        a.getAuthority().equals("GERENTE") ||
                        a.getAuthority().equals("ADMIN"));
    }

    private boolean esPropietario(Long usuarioId, Authentication auth) {
        if (auth == null) {
            return false;
        }

        return usuarioService.obtenerUsuarioPorId(usuarioId)
                .map(usuario ->
                        usuario.getEmail().equals(auth.getName()))
                .orElse(false);
    }

    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<Recompensa> agregarRecompensa(
            @PathVariable Long usuarioId,
            @RequestBody Recompensa recompensa,
            Authentication auth) {

        if (!esAdministrador(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Recompensa nueva = recompensaService.agregarRecompensa(
                usuarioId, recompensa);

        return ResponseEntity.ok(nueva);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recompensa> obtenerRecompensaPorId(
            @PathVariable Long id,
            Authentication auth) {

        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Recompensa> resultado =
                recompensaService.obtenerRecompensaPorId(id);

        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Recompensa recompensa = resultado.get();

        if (!puedeConsultarTodo(auth) &&
                (recompensa.getUsuario() == null ||
                 !esPropietario(recompensa.getUsuario().getId(), auth))) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(recompensa);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Recompensa>> listarRecompensasPorUsuario(
            @PathVariable Long usuarioId,
            Authentication auth) {

        if (!puedeConsultarTodo(auth) &&
                !esPropietario(usuarioId, auth)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                recompensaService.listarRecompensasPorUsuario(usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRecompensa(
            @PathVariable Long id,
            Authentication auth) {

        if (!esAdministrador(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        recompensaService.eliminarRecompensa(id);

        return ResponseEntity.noContent().build();
    }
}
