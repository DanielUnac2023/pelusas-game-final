package edu.unac.backend.controller;

import edu.unac.backend.model.PartidaHistorico;
import edu.unac.backend.service.PartidaHistoricoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PartidaHistoricoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PartidaHistoricoService service;

    @InjectMocks
    private PartidaHistoricoController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testObtenerHistoricoEndpoint() throws Exception {
        PartidaHistorico p1 = new PartidaHistorico("P100", LocalDateTime.now(), "Carlos", 30, "Carlos (30 pts)");
        given(service.obtenerHistorico()).willReturn(Collections.singletonList(p1));

        mockMvc.perform(get("/api/partidas/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].partidaId").value("P100"))
                .andExpect(jsonPath("$[0].ganadorNombre").value("Carlos"));
    }
}