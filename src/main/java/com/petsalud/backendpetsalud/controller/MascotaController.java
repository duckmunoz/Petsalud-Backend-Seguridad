
package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.service.MascotaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;
    private final UsuarioRepository usuarioRepository;

    public MascotaController(
            MascotaService mascotaService,
            UsuarioRepository usuarioRepository) {
        this.mascotaService = mascotaService;
        this.usuarioRepository = usuarioRepository;
    }

    private boolean tieneRol(Authentication auth, String... roles) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        for (String rol : roles) {
            boolean permitido = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(rol));
            if (permitido) {
                return true;
            }
        }
        return false;
    }

    private boolean esPropietario(
            Usuario propietario,
            Authentication auth) {

        return propietario != null
                && auth != null
                && propietario.getEmail() != null
                && propietario.getEmail().equals(auth.getName());
    }

    private boolean puedeConsultar(
            Mascota mascota,
            Authentication auth) {

        return esPropietario(mascota.getPropietario(), auth)
                || tieneRol(auth,
                        "EMPLEADO", "VETERINARIO",
                        "GERENTE", "ADMIN");
    }

    private boolean puedeAdministrar(
            Mascota mascota,
            Authentication auth) {

        return esPropietario(mascota.getPropietario(), auth)
                || tieneRol(auth, "GERENTE", "ADMIN");
    }

    @PostMapping("/propietario/{propietarioId}")
    public ResponseEntity<?> registrarMascota(
            @PathVariable Long propietarioId,
            @RequestBody Mascota mascota,
            Authentication auth) {

        Optional<Usuario> propietario =
                usuarioRepository.findById(propietarioId);

        if (propietario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!esPropietario(propietario.get(), auth)
                && !tieneRol(auth, "GERENTE", "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Mascota nueva =
                mascotaService.registrarMascota(mascota, propietarioId);

        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerMascotaPorId(
            @PathVariable Long id,
            Authentication auth) {

        Optional<Mascota> resultado =
                mascotaService.obtenerMascotaPorId(id);

        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Mascota mascota = resultado.get();

        if (!puedeConsultar(mascota, auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(mascota);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarMascota(
            @PathVariable Long id,
            @RequestBody Mascota datos,
            Authentication auth) {

        Optional<Mascota> resultado =
                mascotaService.obtenerMascotaPorId(id);

        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!puedeAdministrar(resultado.get(), auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Mascota actualizada =
                mascotaService.actualizarMascota(id, datos);

        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMascota(
            @PathVariable Long id,
            Authentication auth) {

        Optional<Mascota> resultado =
                mascotaService.obtenerMascotaPorId(id);

        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!puedeAdministrar(resultado.get(), auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        mascotaService.eliminarMascota(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/propietario/{propietarioId}")
    public ResponseEntity<?> listarMascotasPorPropietario(
            @PathVariable Long propietarioId,
            Authentication auth) {

        Optional<Usuario> propietario =
                usuarioRepository.findById(propietarioId);

        if (propietario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!esPropietario(propietario.get(), auth)
                && !tieneRol(auth,
                        "EMPLEADO", "VETERINARIO",
                        "GERENTE", "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Mascota> mascotas =
                mascotaService.listarMascotasPorPropietario(propietarioId);

        return ResponseEntity.ok(mascotas);
    }
}
