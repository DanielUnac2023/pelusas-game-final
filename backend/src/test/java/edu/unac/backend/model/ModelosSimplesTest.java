package edu.unac.backend.model;

import edu.unac.backend.BackendApplication;
import edu.unac.backend.dto.JugadorDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelosSimplesTest {

    @Test
    void cartaConstructoresYAccesores() {
        Carta c = new Carta(4);
        assertEquals(4, c.getValor());
        assertNull(c.getId());
        Carta d = new Carta(9L, 7);
        assertEquals(9L, d.getId());
        assertEquals(7, d.getValor());
        d.setId(10L);
        d.setValor(3);
        assertEquals(10L, d.getId());
        assertEquals(3, d.getValor());
        assertEquals(0, new Carta().getValor());
    }

    @Test
    void jugadorDtoConstructoresYAccesores() {
        List<Carta> cartas = List.of(new Carta(1));
        JugadorDTO dto = new JugadorDTO(1L, "Ana", cartas, 12);
        assertEquals(1L, dto.getId());
        assertEquals("Ana", dto.getNombre());
        assertEquals(cartas, dto.getCartasDelante());
        assertEquals(12, dto.getPuntosTotales());

        JugadorDTO vacio = new JugadorDTO();
        vacio.setId(2L);
        vacio.setNombre("Beto");
        vacio.setCartasDelante(cartas);
        vacio.setPuntosTotales(3);
        assertEquals(2L, vacio.getId());
        assertEquals("Beto", vacio.getNombre());
        assertSame(cartas, vacio.getCartasDelante());
        assertEquals(3, vacio.getPuntosTotales());
    }

    @Test
    void nombreProhibidoContiene() {
        assertFalse(NombreProhibido.contiene(null));
        assertFalse(NombreProhibido.contiene("Ana"));
        for (String n : new String[]{"popo", "perro", "loca", "estupido", "jorge nitales", "benito camelo"}) {
            assertTrue(NombreProhibido.contiene(n), n);
            assertTrue(NombreProhibido.contiene("  " + n.toUpperCase() + " "), n);
        }
        assertTrue(NombreProhibido.contiene("Benito   Camelo"));
    }

    @Test
    void mainArrancaLaAplicacion() {
        System.setProperty("server.port", "0");
        try {
            assertDoesNotThrow(() -> BackendApplication.main(new String[]{"--server.port=0"}));
        } finally {
            System.clearProperty("server.port");
        }
    }
}
