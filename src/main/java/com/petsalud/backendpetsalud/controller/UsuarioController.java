package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.petsalud.backendpetsalud.dto.UsuarioRegistroRequest;
import com.petsalud.backendpetsalud.dto.UsuarioResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import java.util.Optional;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    
@PostMapping("/registro")
public ResponseEntity<?> registrarUsuario(
        @RequestBody UsuarioRegistroRequest request) {

    if (request.email() == null
            || request.email().isBlank()
            || request.password() == null
            || request.password().isBlank()
            || request.password().length() < 8) {

        return ResponseEntity.badRequest()
                .body("Email y contraseña válida son obligatorios. "
                        + "La contraseña debe tener al menos 8 caracteres.");
    }

    Usuario usuario = new Usuario();

    usuario.setEmail(request.email().trim());
    usuario.setPassword(request.password());
    usuario.setNombre(request.nombre());
    usuario.setApellido(request.apellido());
    usuario.setTelefono(request.telefono());
    usuario.setDireccion(request.direccion());

    try {
        Usuario nuevoUsuario =
                usuarioService.registrarUsuario(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioResponse.desde(nuevoUsuario));

    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("No se pudo registrar el usuario: "
                        + e.getMessage());
    }
}


    

@GetMapping("/{id}")
public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(
        @PathVariable Long id,
        Authentication authentication) {

    // Buscar usuario en la base de datos
    Optional<Usuario> resultado =
            usuarioService.obtenerUsuarioPorId(id);

    // Verificar si existe
    if (resultado.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    Usuario usuario = resultado.get();

    // Verificar autenticacion
    if (authentication == null) {
        return ResponseEntity.status(
                HttpStatus.UNAUTHORIZED).build();
    }

    // Comprobar si consulta su propio perfil
    boolean esPropietario =
            usuario.getEmail().equals(authentication.getName());

    // Comprobar permisos administrativos
    boolean esAdministrador =
            authentication.getAuthorities().stream()
                    .anyMatch(a ->
                            a.getAuthority().equals("ADMIN")
                            || a.getAuthority().equals("GERENTE"));

    // Denegar acceso a perfiles ajenos
    if (!esPropietario && !esAdministrador) {
        return ResponseEntity.status(
                HttpStatus.FORBIDDEN).build();
    }

    // Devolver solamente datos seguros
    return ResponseEntity.ok(
            UsuarioResponse.desde(usuario));
}



    
@PutMapping("/{id}")
public ResponseEntity<UsuarioResponse> actualizarUsuario(
        @PathVariable Long id,
        @RequestBody Usuario usuarioActualizado,
        Authentication authentication) {

    // Comprobar autenticacion
    if (authentication == null) {
        return ResponseEntity.status(
                HttpStatus.UNAUTHORIZED).build();
    }

    // Buscar el usuario que se desea actualizar
    Optional<Usuario> resultado =
            usuarioService.obtenerUsuarioPorId(id);

    if (resultado.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    Usuario usuarioExistente = resultado.get();

    // Verificar si es propietario del perfil
    boolean esPropietario =
            usuarioExistente.getEmail().equals(
                    authentication.getName());

    // Verificar permisos administrativos
    boolean esAdministrador =
            authentication.getAuthorities().stream()
                    .anyMatch(a ->
                            a.getAuthority().equals("GERENTE")
                            || a.getAuthority().equals("ADMIN"));

    // Rechazar modificaciones no autorizadas
    if (!esPropietario && !esAdministrador) {
        return ResponseEntity.status(
                HttpStatus.FORBIDDEN).build();
    }

    // Actualizar los datos permitidos
    Usuario actualizado =
            usuarioService.actualizarUsuario(
                    id, usuarioActualizado);

    // Devolver respuesta sin contrasena
    return ResponseEntity.ok(
            UsuarioResponse.desde(actualizado));
}


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    
@GetMapping
public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {

    List<UsuarioResponse> usuarios =
            usuarioService.listarUsuarios()
                    .stream()
                    .map(UsuarioResponse::desde)
                    .toList();

    return ResponseEntity.ok(usuarios);
}

}
