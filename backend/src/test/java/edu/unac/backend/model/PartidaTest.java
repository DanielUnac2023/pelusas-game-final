package edu.unac.backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PartidaTest {

    private Partida partida;

    @BeforeEach
    void setUp() {
        partida = new Partida();
    }

    @Test
    @DisplayName("Debe inicializar la partida en estado CREADA y con mazo completo")
    void testInicializacionPartida() {
        assertEquals(EstadoPartida.CREADA, partida.getEstado());
        assertEquals(0, partida.getIndiceJugadorActual());
        assertNotNull(partida.getMazo());
        assertEquals(110, partida.getMazo().getCantidadRestante());
        assertTrue(partida.getJugadores().isEmpty());
        assertNull(partida.getJugadorActual());
    }

    @Test
    @DisplayName("Debe probar constructores, id y setters")
    void testConstructoresYSetters() {
        Partida partidaConId = new Partida(10L);
        assertEquals(10L, partidaConId.getId());

        partida.setId(1L);
        assertEquals(1L, partida.getId());

        partida.setEstado(EstadoPartida.EN_PROGRESO);
        assertEquals(EstadoPartida.EN_PROGRESO, partida.getEstado());

        partida.setIndiceJugadorActual(1);
        assertEquals(1, partida.getIndiceJugadorActual());

        List<Jugador> listaJugadores = new ArrayList<>();
        listaJugadores.add(new Jugador("Ana"));
        listaJugadores.add(new Jugador("Carlos"));
        partida.setJugadores(listaJugadores);
        assertEquals(2, partida.getJugadores().size());

        Mazo nuevoMazo = new Mazo();
        partida.setMazo(nuevoMazo);
        assertEquals(nuevoMazo, partida.getMazo());
    }

    @Test
    @DisplayName("Debe rotar los turnos cíclicamente entre los jugadores")
    void testSiguienteTurno() {
        partida.getJugadores().add(new Jugador("Jugador 1"));
        partida.getJugadores().add(new Jugador("Jugador 2"));
        partida.getJugadores().add(new Jugador("Jugador 3"));

        assertEquals("Jugador 1", partida.getJugadorActual().getNombre());

        partida.siguienteTurno();
        assertEquals("Jugador 2", partida.getJugadorActual().getNombre());

        partida.siguienteTurno();
        assertEquals("Jugador 3", partida.getJugadorActual().getNombre());

        // Debe volver al primer jugador (recorrido circular)
        partida.siguienteTurno();
        assertEquals("Jugador 1", partida.getJugadorActual().getNombre());
    }

    @Test
    @DisplayName("Siguiente turno no debe fallar si no hay jugadores")
    void testSiguienteTurnoSinJugadores() {
        assertDoesNotThrow(() -> partida.siguienteTurno());
        assertNull(partida.getJugadorActual());
    }
}