package edu.unac.backend.controller;

import edu.unac.backend.model.*;
import edu.unac.backend.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GameControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    private Partida partidaEjemplo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(gameController).build();

        partidaEjemplo = new Partida();
        partidaEjemplo.setId(1L);
        partidaEjemplo.setEstado(EstadoPartida.EN_PROGRESO);
        partidaEjemplo.setMazo(new Mazo());

        Jugador j1 = new Jugador();
        j1.setId(1L);
        j1.setNombre("Carlos");
        j1.getPozoPuntos().add(new Carta(1L, 5));

        Jugador j2 = new Jugador();
        j2.setId(2L);
        j2.setNombre("Ana");

        partidaEjemplo.setJugadores(Arrays.asList(j1, j2));
    }

    @Test
    @DisplayName("POST /api/partidas - Crear partida")
    void testCrearPartida() throws Exception {
        when(gameService.crearPartida(anyList())).thenReturn(partidaEjemplo);

        mockMvc.perform(post("/api/partidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"Carlos\", \"Ana\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPartida").value(1L))
                .andExpect(jsonPath("$.nombreJugadorActual").value("Carlos"));
    }

    @Test
    @DisplayName("GET /api/partidas/{id} - Obtener partida")
    void testObtenerPartida() throws Exception {
        when(gameService.obtenerPartida(1L)).thenReturn(partidaEjemplo);

        mockMvc.perform(get("/api/partidas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPartida").value(1L));
    }

    @Test
    @DisplayName("POST /api/partidas/{id}/guardar - Guardar pelusas")
    void testGuardarPelusas() throws Exception {
        when(gameService.guardarPelusas(1L)).thenReturn(partidaEjemplo);

        mockMvc.perform(post("/api/partidas/1/guardar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensajeEvento").value("Pelusas guardadas en el pozo seguro."));
    }

    @Test
    @DisplayName("POST /api/partidas/{id}/sacar - Sacar carta")
    void testSacarCarta() throws Exception {
        when(gameService.sacarCarta(1L)).thenReturn(partidaEjemplo);

        mockMvc.perform(post("/api/partidas/1/sacar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensajeEvento").value("Carta sacada del mazo."));
    }

    @Test
    @DisplayName("POST /api/partidas/{id}/robar - Robar cartas exitosamente")
    void testRobarCartasExitoso() throws Exception {
        when(gameService.robarCartas(eq(1L))).thenReturn(partidaEjemplo);

        mockMvc.perform(post("/api/partidas/1/robar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensajeEvento").value("Cartas robadas a rivales con éxito."));
    }

    @Test
    @DisplayName("POST /api/partidas/{id}/robar - Retorna HTTP 400 cuando ocurre una excepción de negocio")
    void testRobarCartasSinCartaARobar() throws Exception {
        // 1. Simular la excepción lanzada por el servicio
        when(gameService.robarCartas(eq(1L)))
                .thenThrow(new IllegalStateException("Debes sacar una carta primero para poder robar."));

        // 2. Simular obtenerPartida para que el catch cree el DTO correctamente
        when(gameService.obtenerPartida(eq(1L))).thenReturn(partidaEjemplo);

        // 3. Verificar status 400 Bad Request y mensaje en el DTO
        mockMvc.perform(post("/api/partidas/1/robar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensajeEvento").value("Debes sacar una carta primero para poder robar."));
    }

    @Test
    @DisplayName("POST /api/partidas/{id}/plantarse - Plantarse")
    void testPlantarse() throws Exception {
        // 1. Asegurar que el jugador actual tenga al menos una carta para no activar el Bad Request
        Jugador jugadorActual = partidaEjemplo.getJugadorActual();
        jugadorActual.getCartasDelante().add(new Carta(1L, 5));

        // 2. Configurar los mocks del servicio
        when(gameService.obtenerPartida(eq(1L))).thenReturn(partidaEjemplo);
        when(gameService.plantarse(eq(1L))).thenReturn(partidaEjemplo);

        // 3. Ejecutar la petición HTTP y validar respuesta 200 OK
        mockMvc.perform(post("/api/partidas/1/plantarse")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensajeEvento").value("Jugador se ha plantado. Cambiando de turno."));
    }

    @Test
    @DisplayName("Cubre las ramas 'false' de mazo == null y jugadorActual == null en convertirADTO")
    void testConvertirADTOConCamposNull() throws Exception {
        Partida partidaSinMazoNiJugadores = new Partida();
        partidaSinMazoNiJugadores.setId(2L);
        partidaSinMazoNiJugadores.setMazo(null); // mazo == null
        partidaSinMazoNiJugadores.setJugadores(new ArrayList<>()); // sin jugadores -> jugadorActual == null

        when(gameService.obtenerPartida(2L)).thenReturn(partidaSinMazoNiJugadores);

        mockMvc.perform(get("/api/partidas/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPartida").value(2L))
                .andExpect(jsonPath("$.cartasRestantesMazo").value(0));
    }
    @Test
    void testObtenerHistorial() throws Exception {
        PartidaHistorico historico = new PartidaHistorico("1", LocalDateTime.now(), "Carlos", 25, "Carlos (25 pts), Ana (10 pts)");
        when(gameService.obtenerHistorial()).thenReturn(List.of(historico));

        mockMvc.perform(get("/api/partidas/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].partidaId").value("1"))
                .andExpect(jsonPath("$[0].ganadorNombre").value("Carlos"))
                .andExpect(jsonPath("$[0].ganadorPuntaje").value(25));
    }

}