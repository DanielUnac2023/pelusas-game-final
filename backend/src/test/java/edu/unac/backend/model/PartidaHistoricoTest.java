package edu.unac.backend.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PartidaHistoricoTest {

    @Test
    @DisplayName("Debe verificar el constructor completo, getters y setters")
    void testPartidaHistoricoGettersYSetters() {
        LocalDateTime ahora = LocalDateTime.now();
        PartidaHistorico historico = new PartidaHistorico("10", ahora, "Ana", 30, "Ana (30 pts), Juan (15 pts)");

        assertEquals("10", historico.getPartidaId());
        assertEquals(ahora, historico.getFechaFin());
        assertEquals("Ana", historico.getGanadorNombre());
        assertEquals(30, historico.getGanadorPuntaje());
        assertEquals("Ana (30 pts), Juan (15 pts)", historico.getResumenJugadores());

        // Probar setters e ID
        historico.setId(1L);
        historico.setPartidaId("20");
        historico.setGanadorNombre("Carlos");
        historico.setGanadorPuntaje(40);
        historico.setResumenJugadores("Carlos (40 pts)");

        assertEquals(1L, historico.getId());
        assertEquals("20", historico.getPartidaId());
        assertEquals("Carlos", historico.getGanadorNombre());
        assertEquals(40, historico.getGanadorPuntaje());
        assertEquals("Carlos (40 pts)", historico.getResumenJugadores());
    }

    @Test
    @DisplayName("Debe verificar el constructor vacío")
    void testConstructorVacio() {
        PartidaHistorico historico = new PartidaHistorico();
        assertNull(historico.getId());
        assertNull(historico.getPartidaId());
    }
}