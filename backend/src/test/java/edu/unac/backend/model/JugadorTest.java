package edu.unac.backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JugadorTest {

    private Jugador jugador;

    @BeforeEach
    void setUp() {
        jugador = new Jugador("Carlos");
    }

    @Test
    @DisplayName("Debe inicializar el jugador con nombre y colecciones vacías")
    void testInicializacionJugador() {
        assertEquals("Carlos", jugador.getNombre());
        assertTrue(jugador.getCartasDelante().isEmpty());
        assertTrue(jugador.getPozoPuntos().isEmpty());
        assertEquals(0, jugador.getPuntuacionTotal());
    }

    @Test
    @DisplayName("Debe probar constructores alternativos, id y setters")
    void testConstructoresYSetters() {
        Jugador jugadorVacio = new Jugador();
        assertNull(jugadorVacio.getNombre());

        Jugador jugadorConId = new Jugador(1L, "Ana");
        assertEquals(1L, jugadorConId.getId());
        assertEquals("Ana", jugadorConId.getNombre());

        jugador.setId(2L);
        assertEquals(2L, jugador.getId());

        jugador.setNombre("Pedro");
        assertEquals("Pedro", jugador.getNombre());

        List<Carta> nuevasDelante = new ArrayList<>();
        nuevasDelante.add(new Carta(1L, 3));
        jugador.setCartasDelante(nuevasDelante);
        assertEquals(1, jugador.getCartasDelante().size());

        List<Carta> nuevoPozo = new ArrayList<>();
        nuevoPozo.add(new Carta(2L, 7));
        jugador.setPozoPuntos(nuevoPozo);
        assertEquals(1, jugador.getPozoPuntos().size());
    }

    @Test
    @DisplayName("Debe calcular la puntuación total sumando las cartas del pozo de puntos")
    void testPuntuacionTotal() {
        jugador.getPozoPuntos().add(new Carta(1L, 5));
        jugador.getPozoPuntos().add(new Carta(2L, 8));
        jugador.getPozoPuntos().add(new Carta(3L, 2));

        assertEquals(15, jugador.getPuntuacionTotal(), "La suma de 5 + 8 + 2 debe ser 15");
    }

    @Test
    @DisplayName("Las cartas delante (en riesgo) no deben sumar a la puntuación total")
    void testCartasDelanteNoSumanPuntos() {
        jugador.getCartasDelante().add(new Carta(1L, 10));

        assertEquals(0, jugador.getPuntuacionTotal(), "Las cartas delante no cuentan como puntos hasta guardarse");
    }
}