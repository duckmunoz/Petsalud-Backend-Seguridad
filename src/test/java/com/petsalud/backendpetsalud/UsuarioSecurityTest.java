
package com.petsalud.backendpetsalud;

import com.petsalud.backendpetsalud.controller.UsuarioController;
import com.petsalud.backendpetsalud.service.UsuarioService;
import com.petsalud.backendpetsalud.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    // Simulamos el filtro para evitar dependencias JWT
    // en esta prueba aislada del controlador.
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void listarUsuariosDevuelve200() throws Exception {

        when(usuarioService.listarUsuarios())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }
}
