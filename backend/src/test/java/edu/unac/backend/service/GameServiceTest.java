package edu.unac.backend.service;

import edu.unac.backend.model.Carta;
import edu.unac.backend.model.EstadoPartida;
import edu.unac.backend.model.Jugador;
import edu.unac.backend.model.Partida;
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
import static org.mockito.Mockito.when;

class GameServiceTest {

    @Mock
    private PartidaRepository partidaRepository;

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
        // 1. Obtener el jugador actual e incrustar la carta de prueba
        Jugador actual = partida.getJugadorActual();
        actual.getCartasDelante().add(new Carta(1L, 5));

        // 2. Ejecutar la regla de negocio a través del servicio
        Partida resultado = gameService.guardarPelusas(1L);

        // 3. Obtener la referencia actualizada del jugador desde el resultado devuelto
        Jugador jugadorActualizado = resultado.getJugadorActual();

        // 4. Aserciones sobre el estado devuelto por el servicio
        assertTrue(jugadorActualizado.getCartasDelante().isEmpty());
        assertEquals(1, jugadorActualizado.getPozoPuntos().size());
        assertEquals(5, jugadorActualizado.getPozoPuntos().get(0).getValor());
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
    @DisplayName("Debe robar cartas coincidentes con la última carta sacada")
    void testRobarCartas() {
        Jugador actual = partida.getJugadorActual();
        Jugador oponente = partida.getJugadores().get(1);

        // Oponente tiene en mesa una carta con valor 8
        oponente.getCartasDelante().add(new Carta(1L, 8));

        // Establecer la última carta sacada en el turno activo
        partida.setValorUltimaCarta(8);

        // Invocación con 1 solo parámetro
        gameService.robarCartas(1L);

        assertTrue(oponente.getCartasDelante().isEmpty());
        assertEquals(1, actual.getCartasDelante().size());
        assertEquals(8, actual.getCartasDelante().get(0).getValor());
    }

    @Test
    @DisplayName("Debe lanzar excepción si se intenta robar sin haber sacado carta previamente")
    void testRobarCartasSinSacarCartaPrevia() {
        partida.setValorUltimaCarta(null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gameService.robarCartas(1L)
        );

        assertEquals("Debes sacar una carta primero para poder robar.", exception.getMessage());
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
    @DisplayName("Debe lanzar excepción al intentar guardar pelusas si no tiene cartas delante")
    void testGuardarPelusasConMesaVacia() {
        // 1. Configuración previa (ejemplo: partidaId = 1L)
        Long partidaId = 1L;

        // Asegurarse de que el jugador actual no tenga cartas en 'cartasDelante'
        Jugador actual = partida.getJugadorActual();
        actual.getCartasDelante().clear();

        // 2. Verificar que el servicio lance IllegalStateException
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gameService.guardarPelusas(partidaId)
        );

        // 3. (Opcional) Validar que el mensaje coincida exactamente con el de la regla de negocio
        assertEquals("No tienes cartas delante para guardar.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe robar solo las cartas del valor especificado e ignorar las demás")
    void testRobarCartasIgnoraValoresDiferentes() {
        Jugador actual = partida.getJugadorActual();
        Jugador oponente = partida.getJugadores().stream()
                .filter(j -> !j.getId().equals(actual.getId()))
                .findFirst().orElseThrow();

        oponente.getCartasDelante().add(new Carta(1L, 8));
        oponente.getCartasDelante().add(new Carta(2L, 3));

        // Establecer la última carta sacada a 8
        partida.setValorUltimaCarta(8);

        // Invocación con 1 solo parámetro
        Partida resultado = gameService.robarCartas(1L);

        Jugador oponenteActualizado = resultado.getJugadores().stream()
                .filter(j -> j.getId().equals(oponente.getId()))
                .findFirst().orElseThrow();

        assertTrue(oponenteActualizado.getCartasDelante().stream().noneMatch(c -> c.getValor() == 8));
        assertTrue(oponenteActualizado.getCartasDelante().stream().anyMatch(c -> c.getValor() == 3));
    }

    @Test
    @DisplayName("Debe robar TODAS las cartas del mismo valor si un oponente tiene múltiples cartas iguales")
    void testRobarMultiplesCartasMismoValor() {
        Jugador actual = partida.getJugadorActual();
        Jugador oponente = partida.getJugadores().stream()
                .filter(j -> !j.getId().equals(actual.getId()))
                .findFirst().orElseThrow();

        // 1. Asignar DOS cartas con el mismo valor (ej: valor 3) al oponente
        oponente.getCartasDelante().add(new Carta(10L, 3));
        oponente.getCartasDelante().add(new Carta(11L, 3));

        // 2. Establecer que el jugador actual sacó una carta de valor 3
        partida.setValorUltimaCarta(3);

        // 3. Ejecutar el robo con la firma de 1 solo parámetro
        Partida resultado = gameService.robarCartas(partida.getId());

        Jugador oponenteActualizado = resultado.getJugadores().stream()
                .filter(j -> j.getId().equals(oponente.getId()))
                .findFirst().orElseThrow();

        // 4. Aserciones:
        // A. El oponente debe quedarse sin cartas de valor 3
        assertTrue(oponenteActualizado.getCartasDelante().stream().noneMatch(c -> c.getValor() == 3));

        // B. El jugador actual debe tener las 2 cartas robadas de valor 3
        long cantidadTresesActual = resultado.getJugadorActual().getCartasDelante().stream()
                .filter(c -> c.getValor() == 3)
                .count();
        assertEquals(2, cantidadTresesActual);
    }
}