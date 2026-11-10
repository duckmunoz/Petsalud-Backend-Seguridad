
package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Recompensa;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.RecompensaRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RecompensaServiceImpl implements RecompensaService {

    @Autowired
    private RecompensaRepository recompensaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @PreAuthorize("hasAnyAuthority('GERENTE', 'ADMIN')")
    public Recompensa agregarRecompensa(Long usuarioId, Recompensa recompensa) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        recompensa.setUsuario(usuario);
        recompensa.setFecha(LocalDateTime.now());

        return recompensaRepository.save(recompensa);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('GERENTE', 'ADMIN') or " +
            "@usuarioSecurity.esPropietario(#id, authentication.name, 'recompensa')")
    public Optional<Recompensa> obtenerRecompensaPorId(Long id) {
        return recompensaRepository.findById(id);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('EMPLEADO', 'GERENTE', 'ADMIN') or " +
            "@usuarioSecurity.esUsuarioActual(#usuarioId, authentication.name)")
    public List<Recompensa> listarRecompensasPorUsuario(Long usuarioId) {
        return recompensaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('GERENTE', 'ADMIN')")
    public void eliminarRecompensa(Long id) {
        recompensaRepository.deleteById(id);
    }
}
