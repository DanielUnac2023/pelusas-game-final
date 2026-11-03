package edu.unac.backend.service;

import edu.unac.backend.model.*;
import edu.unac.backend.repository.PartidaHistoricoRepository;
import edu.unac.backend.repository.PartidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GameServiceTest {

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private PartidaHistoricoRepository partidaHistoricoRepository;

    @InjectMocks
    private GameService gameService;

    private Partida partida;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Crear una partida válida con 2 jugadores para los tests
        partida = new Partida();
        partida.setId(1L);
        partida.setEstado(EstadoPartida.EN_PROGRESO);

        Jugador j1 = new Jugador();
        j1.setId(1L);
        j1.setNombre("Jugador 1");

        Jugador j2 = new Jugador();
        j2.setId(2L);
        j2.setNombre("Jugador 2");

        partida.setJugadores(Arrays.asList(j1, j2));

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    @DisplayName("Debe crear partida correctamente")
    void testCrearPartidaExitosamente() {
        Partida nueva = gameService.crearPartida(Arrays.asList("Ana", "Carlos"));
        assertNotNull(nueva);
        assertEquals(2, nueva.getJugadores().size());
        assertEquals(EstadoPartida.EN_PROGRESO, nueva.getEstado());
    }

    @Test
    @DisplayName("Debe fallar si intenta crear partida con menos de 2 o más de 6 jugadores")
    void testCrearPartidaCantidadInvalida() {
        assertThrows(IllegalArgumentException.class, () -> gameService.crearPartida(List.of("Solo uno")));
        assertThrows(IllegalArgumentException.class, () -> gameService.crearPartida(
                Arrays.asList("1", "2", "3", "4", "5", "6", "7")
        ));
    }

    @Test
    @DisplayName("Debe mover cartasDelante a pozoPuntos al guardar pelusas")
    void testGuardarPelusas() {
        Jugador actual = partida.getJugadorActual();
        actual.getCartasDelante().add(new Carta(1L, 5));

        Partida resultado = gameService.guardarPelusas(1L);

        assertTrue(actual.getCartasDelante().isEmpty());
        assertEquals(1, actual.getPozoPuntos().size());
        assertEquals(5, actual.getPozoPuntos().get(0).getValor());
    }

    @Test
    @DisplayName("Debe sacar carta y agregarla a la mesa si no hay pérdida de turno")
    void testSacarCartaSinPerdidaTurno() {
        Jugador actual = partida.getJugadorActual();

        Partida res = gameService.sacarCarta(1L);

        assertNotNull(res);
        assertEquals(1, actual.getCartasDelante().size());
    }

    @Test
    @DisplayName("Debe activar Pérdida de Turno al tener >= 3 cartas y sacar un valor repetido")
    void testSacarCartaPerdidaDeTurno() {
        Jugador actual = partida.getJugadorActual();

        // Simular que el jugador ya tiene 3 cartas en mesa
        actual.getCartasDelante().add(new Carta(1L, 4));
        actual.getCartasDelante().add(new Carta(2L, 5));
        actual.getCartasDelante().add(new Carta(3L, 6));

        // Forzar que la siguiente carta sacada sea un 4 (repetido)
        partida.getMazo().getCartas().add(0, new Carta(4L, 4));

        Partida res = gameService.sacarCarta(1L);

        // Se deben limpiar las cartas y pasar al siguiente turno
        assertTrue(actual.getCartasDelante().isEmpty());
        assertEquals(1, res.getIndiceJugadorActual());
    }

    @Test
    @DisplayName("Debe robar cartas coincidentes de los rivales")
    void testRobarCartas() {
        Jugador actual = partida.getJugadorActual();
        Jugador oponente = partida.getJugadores().get(1);

        // Oponente tiene en mesa una carta con valor 8
        oponente.getCartasDelante().add(new Carta(1L, 8));

        gameService.robarCartas(1L, 8);

        assertTrue(oponente.getCartasDelante().isEmpty());
        assertEquals(1, actual.getCartasDelante().size());
        assertEquals(8, actual.getCartasDelante().get(0).getValor());
    }

    @Test
    @DisplayName("Debe avanzar el turno al plantarse")
    void testPlantarse() {
        assertEquals(0, partida.getIndiceJugadorActual());

        Partida res = gameService.plantarse(1L);

        assertEquals(1, res.getIndiceJugadorActual());
    }

    @Test
    @DisplayName("Debe lanzar excepción si se busca una partida que no existe")
    void testObtenerPartidaNoExiste() {
        when(partidaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> gameService.sacarCarta(99L));
    }

    @Test
    @DisplayName("Debe lanzar excepción si la partida no está en progreso")
    void testPartidaNoEnProgreso() {
        partida.setEstado(EstadoPartida.FINALIZADA);
        assertThrows(IllegalStateException.class, () -> gameService.plantarse(1L));
    }

    @Test
    @DisplayName("NO debe perder turno si tiene menos de 3 cartas en mesa aunque el valor sea repetido")
    void testSacarCartaRepetidaConMenosDe3CartasSinPerdidaTurno() {
        Jugador actual = partida.getJugadorActual();

        // El jugador solo tiene 2 cartas en mesa
        actual.getCartasDelante().add(new Carta(1L, 5));
        actual.getCartasDelante().add(new Carta(2L, 6));

        // Forzar que la siguiente carta sacada sea un 5 (repetido, pero solo hay 2 cartas)
        partida.getMazo().getCartas().add(0, new Carta(3L, 5));

        Partida res = gameService.sacarCarta(1L);

        // No pierde el turno y ahora tiene 3 cartas en mesa
        assertEquals(3, actual.getCartasDelante().size());
        assertEquals(0, res.getIndiceJugadorActual());
    }

    @Test
    @DisplayName("Debe finalizar la partida si el mazo se vacía")
    void testSacarCartaMazoVacioFinalizaPartida() {
        // Vaciar el mazo manualmente
        partida.getMazo().getCartas().clear();

        Partida res = gameService.sacarCarta(1L);

        assertEquals(EstadoPartida.FINALIZADA, res.getEstado());
    }

    @Test
    @DisplayName("Debe retornar la partida correctamente al consultar por ID")
    void testObtenerPartidaExitoso() {
        Partida res = gameService.obtenerPartida(1L);
        assertNotNull(res);
        assertEquals(1L, res.getId());
    }

    @Test
    @DisplayName("Debe evaluar la rama de mazo agotado (0 cartas) al sacar la última carta")
    void testSacarUltimaCartaDelMazo() {
        // 1. Dejar el mazo con exactamente 1 carta
        partida.getMazo().getCartas().clear();
        partida.getMazo().getCartas().add(new Carta(99L, 3));

        // 2. Sacar esa última carta (deja la cantidad restante en 0)
        Partida res = gameService.sacarCarta(1L);

        // 3. Verificar que se activó la condición de la línea 93 y se finalizó la partida
        assertEquals(0, partida.getMazo().getCantidadRestante());
        assertEquals(EstadoPartida.FINALIZADA, res.getEstado());
    }

    @Test
    @DisplayName("Debe lanzar excepción si la lista de nombres de jugadores es null")
    void testCrearPartidaConListaNull() {
        assertThrows(IllegalArgumentException.class, () -> gameService.crearPartida(null));
    }

    @Test
    @DisplayName("No debe hacer nada al guardar pelusas si cartasDelante está vacía")
    void testGuardarPelusasConMesaVacia() {
        Jugador actual = partida.getJugadorActual();
        actual.getCartasDelante().clear(); // Asegurar que está vacía

        Partida res = gameService.guardarPelusas(1L);

        assertNotNull(res);
        assertTrue(actual.getPozoPuntos().isEmpty());
    }

    @Test
    @DisplayName("Debe ignorar cartas del oponente que no coinciden al robar")
    void testRobarCartasIgnoraValoresDiferentes() {
        Jugador actual = partida.getJugadorActual();
        Jugador oponente = partida.getJugadores().get(1);

        // Oponente tiene una carta valor 5 y el jugador busca robar valor 8
        oponente.getCartasDelante().add(new Carta(1L, 5));

        gameService.robarCartas(1L, 8);

        // La carta debe continuar en la mesa del oponente
        assertEquals(1, oponente.getCartasDelante().size());
        assertTrue(actual.getCartasDelante().isEmpty());
    }

    @Test
    @DisplayName("Debe obtener el historial de partidas guardadas")
    void testObtenerHistorial() {
        PartidaHistorico item = new PartidaHistorico();
        when(partidaHistoricoRepository.findAll()).thenReturn(List.of(item));

        List<PartidaHistorico> resultado = gameService.obtenerHistorial();

        assertEquals(1, resultado.size());
        verify(partidaHistoricoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe manejar el caso de finalización cuando no hay ganador (Ganador Null / Empate)")
    void testFinalizarPartidaGanadorNull() {
        // Para forzar ganador null, inicializamos sin jugadores o con puntos por defecto
        Partida p = new Partida();
        p.setId(2L);
        p.setEstado(EstadoPartida.EN_PROGRESO);
        p.setJugadores(List.of()); // Lista vacía para que maxPuntos quede en -1 y ganador sea null

        when(partidaRepository.findById(2L)).thenReturn(Optional.of(p));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(i -> i.getArguments()[0]);

        // Al sacar la última carta con mazo en 0, disparará finalizarPartida con ganador = null
        p.getMazo().getCartas().clear();
        gameService.sacarCarta(2L);

        verify(partidaHistoricoRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("No debe perder el turno si tiene 3 o más cartas pero el valor sacado NO es repetido")
    void testSacarCartaMasDeTresCartasValorNoRepetido() {
        Jugador actual = partida.getJugadorActual();
        actual.getCartasDelante().add(new Carta(1L, 1));
        actual.getCartasDelante().add(new Carta(2L, 2));
        actual.getCartasDelante().add(new Carta(3L, 3));

        // Forzar sacar una carta de valor 4 (no existente en la mesa)
        partida.getMazo().getCartas().add(0, new Carta(4L, 4));

        Partida res = gameService.sacarCarta(1L);

        // No pierde turno y se agrega la carta a sus 3 anteriores (quedan 4)
        assertEquals(4, actual.getCartasDelante().size());
        assertEquals(0, res.getIndiceJugadorActual()); // Mantiene el turno
    }

    @Test
    @DisplayName("No debe perder el turno si tiene menos de 3 cartas aunque el valor sea repetido")
    void testSacarCartaMenosDeTresCartasValorRepetido() {
        Jugador actual = partida.getJugadorActual();
        actual.getCartasDelante().add(new Carta(1L, 5));
        actual.getCartasDelante().add(new Carta(2L, 6)); // Tiene solo 2 cartas

        // Forzar sacar una carta de valor 5 (repetido)
        partida.getMazo().getCartas().add(0, new Carta(3L, 5));

        Partida res = gameService.sacarCarta(1L);

        // No pierde turno porque tenía < 3 cartas
        assertEquals(3, actual.getCartasDelante().size());
        assertEquals(0, res.getIndiceJugadorActual());
    }

    @Test
    @DisplayName("Debe evaluar anyMatch false cuando el jugador tiene cartas pero ninguna coincide")
    void testStreamAnyMatchConCartasDiferentes() {
        Jugador actual = partida.getJugadorActual();
        actual.getCartasDelante().add(new Carta(1L, 1));

        // Forzar sacar valor 2
        partida.getMazo().getCartas().add(0, new Carta(2L, 2));

        gameService.sacarCarta(1L);

        assertEquals(2, actual.getCartasDelante().size());
    }

    @Test
    @DisplayName("Debe desempatar asignando el ganador a quien tenga mas cartas de valor 1 cuando empatan en puntos")
    void testDesempatePorCantidadDeCartas() {
        Partida p = new Partida();
        p.setId(3L);
        p.setEstado(EstadoPartida.EN_PROGRESO);

        // Jugador 1 suma 2 puntos con dos cartas de valor 1 -> [1, 1]
        Jugador j1 = new Jugador();
        j1.setId(1L);
        j1.setNombre("Jugador 1");
        j1.getPozoPuntos().add(new Carta(1L, 1));
        j1.getPozoPuntos().add(new Carta(2L, 1));

        // Jugador 2 suma 2 puntos con una carta de valor 2 -> [2]
        Jugador j2 = new Jugador();
        j2.setId(2L);
        j2.setNombre("Jugador 2");
        j2.getPozoPuntos().add(new Carta(3L, 2));

        p.setJugadores(List.of(j1, j2));
        p.getMazo().getCartas().clear(); // Forzar vaciado de mazo

        when(partidaRepository.findById(3L)).thenReturn(Optional.of(p));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(i -> i.getArguments()[0]);

        gameService.sacarCarta(3L);

        // Jugador 1 debe ganar por tener dos cartas de valor 1 frente a cero de Jugador 2
        verify(partidaHistoricoRepository, times(1)).save(argThat(h ->
                "Jugador 1".equals(h.getGanadorNombre())
        ));
    }
}