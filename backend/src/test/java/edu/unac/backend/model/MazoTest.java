package edu.unac.backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MazoTest {

    private Mazo mazo;

    @BeforeEach
    void setUp() {
        mazo = new Mazo();
    }

    @Test
    @DisplayName("Debe crear exactamente 110 cartas en el mazo")
    void testCantidadTotalCartas() {
        assertEquals(110, mazo.getCantidadRestante());
    }

    @Test
    @DisplayName("Debe tener la distribución correcta: 65 cartas (1-5) y 45 cartas (6-10)")
    void testDistribucionCartas() {
        long cartas1a5 = mazo.getCartas().stream()
                .filter(c -> c.getValor() >= 1 && c.getValor() <= 5)
                .count();

        long cartas6a10 = mazo.getCartas().stream()
                .filter(c -> c.getValor() >= 6 && c.getValor() <= 10)
                .count();

        assertEquals(65, cartas1a5, "Debe haber 65 cartas de valores 1 al 5");
        assertEquals(45, cartas6a10, "Debe haber 45 cartas de valores 6 al 10");
    }

    @Test
    @DisplayName("Debe extraer cartas correctamente reduciendo el conteo")
    void testSacarCarta() {
        Carta carta = mazo.sacarCarta();
        assertNotNull(carta);
        assertEquals(109, mazo.getCantidadRestante());
    }

    @Test
    @DisplayName("Debe verificar que el mazo realmente cambie el orden al mezclarse")
    void testMezclarCambiaOrden() {
        // Mazo recién creado (ya viene mezclado por el constructor)
        List<Integer> valoresPrimerMezcla = mazo.getCartas().stream()
                .map(Carta::getValor)
                .toList();

        // Volvemos a mezclar
        mazo.mezclar();

        List<Integer> valoresSegundaMezcla = mazo.getCartas().stream()
                .map(Carta::getValor)
                .toList();

        // Comprobamos que el orden cambia entre mezclas
        assertNotEquals(valoresPrimerMezcla, valoresSegundaMezcla, "El orden de las cartas debió haber cambiado tras mezclar");
    }

    @Test
    @DisplayName("Debe retornar null al intentar sacar carta de un mazo vacío")
    void testSacarCartaMazoVacio() {
        // Vaciamos las 110 cartas
        for (int i = 0; i < 110; i++) {
            mazo.sacarCarta();
        }

        assertEquals(0, mazo.getCantidadRestante());
        assertNull(mazo.sacarCarta(), "Sacar carta de un mazo vacío debe retornar null");
    }
}