
package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Usuario registrarUsuario(Usuario usuario) {

        // Verificar si el correo ya existe
        Optional<Usuario> usuarioExistente =
                usuarioRepository.findByEmail(usuario.getEmail());

        if (usuarioExistente.isPresent()) {
            throw new IllegalArgumentException(
                    "El email ya está registrado"
            );
        }

        // Cifrar la contraseña utilizando BCrypt
        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

        // Asignar el rol CLIENTE por defecto
        usuario.setRol(Rol.CLIENTE);

        // Evitar que el cliente establezca el ID
        usuario.setId(null);

        // Guardar usuario
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public Usuario actualizarUsuario(
            Long id, Usuario usuarioActualizado) {

        return usuarioRepository.findById(id).map(usuario -> {

            usuario.setNombre(usuarioActualizado.getNombre());
            usuario.setApellido(usuarioActualizado.getApellido());
            usuario.setTelefono(usuarioActualizado.getTelefono());
            usuario.setDireccion(usuarioActualizado.getDireccion());

            // No modificar email, contraseña ni rol
            return usuarioRepository.save(usuario);

        }).orElseThrow(() ->
                new IllegalArgumentException("Usuario no encontrado")
        );
    }

    @Override
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}
