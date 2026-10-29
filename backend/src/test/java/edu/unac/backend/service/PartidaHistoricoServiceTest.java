package edu.unac.backend.service;

import edu.unac.backend.model.PartidaHistorico;
import edu.unac.backend.repository.PartidaHistoricoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PartidaHistoricoServiceTest {

    @Mock
    private PartidaHistoricoRepository repository;

    @InjectMocks
    private PartidaHistoricoService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testObtenerHistorico() {
        PartidaHistorico p1 = new PartidaHistorico("P1", LocalDateTime.now(), "Juan", 20, "Juan (20 pts)");
        when(repository.findAll()).thenReturn(Arrays.asList(p1));

        List<PartidaHistorico> resultado = service.obtenerHistorico();

        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getGanadorNombre());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGuardarHistorico() {
        PartidaHistorico p1 = new PartidaHistorico("P1", LocalDateTime.now(), "Maria", 15, "Maria (15 pts)");
        when(repository.save(any(PartidaHistorico.class))).thenReturn(p1);

        PartidaHistorico guardado = service.guardarHistorico(p1);

        assertNotNull(guardado);
        assertEquals("Maria", guardado.getGanadorNombre());
        verify(repository, times(1)).save(p1);
    }
}