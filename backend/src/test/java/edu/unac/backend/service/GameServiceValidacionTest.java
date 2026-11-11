package edu.unac.backend.service;

import edu.unac.backend.model.EstadoPartida;
import edu.unac.backend.model.Partida;
import edu.unac.backend.repository.PartidaHistoricoRepository;
import edu.unac.backend.repository.PartidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Pruebas unitarias de la validación de nombres y del desempate sin jugadores. */
class GameServiceValidacionTest {

    @Mock
    private PartidaRepository partidaRepository;
    @Mock
    private PartidaHistoricoRepository partidaHistoricoRepository;
    @InjectMocks
    private GameService gameService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(partidaRepository.save(any(Partida.class))).thenAnswer(i -> i.getArguments()[0]);
    }

    private String mensajeDe(String... nombres) {
        return assertThrows(IllegalArgumentException.class,
                () -> gameService.crearPartida(Arrays.asList(nombres))).getMessage();
    }

    @Test
    void rechazaNombreNulo() {
        assertEquals("El nombre del jugador no puede ser nulo.", mensajeDe("Ana", null));
    }

    @Test
    void rechazaNombreMuyCorto() {
        assertEquals("El nombre 'Jo' tiene menos de 3 caracteres.", mensajeDe("Ana", " Jo "));
    }

    @Test
    void rechazaNombreMuyLargo() {
        assertEquals("El nombre 'abcdefghijklmnop' tiene más de 15 caracteres.",
                mensajeDe("Ana", "abcdefghijklmnop"));
    }

    @Test
    void aceptaNombresEnLosLimites() {
        Partida p = gameService.crearPartida(Arrays.asList("abc", "abcdefghijklmno"));
        assertEquals("abc", p.getJugadores().get(0).getNombre());
        assertEquals("abcdefghijklmno", p.getJugadores().get(1).getNombre());
    }

    @Test
    void rechazaNombreProhibidoSinImportarMayusculas() {
        assertEquals("El nombre 'POPO' no está permitido.", mensajeDe("Ana", "POPO"));
        assertEquals("El nombre 'Jorge  Nitales' no está permitido.", mensajeDe("Ana", "Jorge  Nitales"));
    }

    @Test
    void rechazaListaNula() {
        assertThrows(IllegalArgumentException.class, () -> gameService.crearPartida(null));
    }

    @Test
    void sacarConMazoVacioYSinJugadoresFinalizaSinGanador() {
        Partida p = new Partida();
        p.setId(7L);
        p.setEstado(EstadoPartida.EN_PROGRESO);
        p.setJugadores(new ArrayList<>());
        p.getMazo().getCartas().clear();
        when(partidaRepository.findById(7L)).thenReturn(Optional.of(p));

        Partida r = gameService.sacarCarta(7L);

        assertEquals(EstadoPartida.FINALIZADA, r.getEstado());
        assertTrue(r.getJugadores().isEmpty());
    }
}
