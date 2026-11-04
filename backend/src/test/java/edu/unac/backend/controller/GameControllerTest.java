package edu.unac.backend.controller;

import edu.unac.backend.model.Carta;
import edu.unac.backend.model.EstadoPartida;
import edu.unac.backend.model.Jugador;
import edu.unac.backend.model.Partida;
import edu.unac.backend.model.PartidaHistorico;
import edu.unac.backend.repository.PartidaHistoricoRepository;
import edu.unac.backend.repository.PartidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerIntegrationTest {

    private static final String BASE = "/api/partidas";
    private static final int[] NADA = {};
    private static final String MSG_NO_EN_CURSO = "La partida no está en curso.";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private PartidaRepository partidaRepository;
    @Autowired
    private PartidaHistoricoRepository historicoRepository;

    @BeforeEach
    void limpiar() {
        partidaRepository.deleteAll();
        historicoRepository.deleteAll();
    }

    // ------------------------------------------------------------------ helpers

    private static Jugador jugador(String nombre, int[] delante, int[] pozo) {
        Jugador j = new Jugador(nombre);
        for (int v : delante) j.getCartasDelante().add(new Carta(v));
        for (int v : pozo) j.getPozoPuntos().add(new Carta(v));
        return j;
    }

    /** Construye una partida con mazo conocido (el orden de salida es el del arreglo). */
    private static Partida construir(EstadoPartida estado, int indice, int[] mazo, Jugador... js) {
        Partida p = new Partida();
        p.setEstado(estado);
        p.setIndiceJugadorActual(indice);
        p.setJugadores(new ArrayList<>(List.of(js)));
        p.getMazo().getCartas().clear();
        for (int v : mazo) p.getMazo().getCartas().add(new Carta(v));
        return p;
    }

    private Long guardar(Partida p) {
        return partidaRepository.save(p).getId();
    }

    private Long sembrar(int indice, int[] mazo, Jugador... js) {
        return guardar(construir(EstadoPartida.EN_PROGRESO, indice, mazo, js));
    }

    private static String nombres(int n) {
        return IntStream.rangeClosed(1, n).mapToObj(i -> "\"J" + i + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static MockHttpServletRequestBuilder crear(String json) {
        return post(BASE).contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private ResultActions ok(MockHttpServletRequestBuilder req) throws Exception {
        return mvc.perform(req).andExpect(status().isOk());
    }

    private void assertFalla(MockHttpServletRequestBuilder req, Class<? extends Throwable> tipo, String msg) {
        assertThatThrownBy(() -> mvc.perform(req))
                .rootCause().isInstanceOf(tipo).hasMessage(msg);
    }

    private PartidaHistorico unicoHistorico() {
        List<PartidaHistorico> todos = historicoRepository.findAll();
        assertThat(todos).hasSize(1);
        return todos.get(0);
    }

    /** Parte con mazo vacío: el primer /sacar finaliza la partida. */
    private Long finalizarConMazoVacio(Jugador... js) throws Exception {
        Long id = sembrar(0, NADA, js);
        ok(post(BASE + "/" + id + "/sacar")).andExpect(jsonPath("$.estadoPartida").value("FINALIZADA"));
        return id;
    }

    // ------------------------------------------------------------------ crear

    @Test
    void crearPartida_devuelveEstadoInicialCompleto() throws Exception {
        ok(crear("[\"Ana\",\"Beto\"]"))
                .andExpect(jsonPath("$.idPartida").isNumber())
                .andExpect(jsonPath("$.estadoPartida").value("EN_PROGRESO"))
                .andExpect(jsonPath("$.cartasRestantesMazo").value(110))
                .andExpect(jsonPath("$.jugadorActualIndex").value(0))
                .andExpect(jsonPath("$.nombreJugadorActual").value("Ana"))
                .andExpect(jsonPath("$.mensajeEvento").value("Partida creada exitosamente."))
                .andExpect(jsonPath("$.accionesPermitidas.length()").value(4))
                .andExpect(jsonPath("$.accionesPermitidas[0]").value("SACAR_CARTA"))
                .andExpect(jsonPath("$.accionesPermitidas[1]").value("PLANTARSE"))
                .andExpect(jsonPath("$.accionesPermitidas[2]").value("GUARDAR_PELUSAS"))
                .andExpect(jsonPath("$.accionesPermitidas[3]").value("ROBAR"));

        assertThat(partidaRepository.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 6})
    void crearPartida_aceptaLimitesDeJugadores(int n) throws Exception {
        ok(crear(nombres(n))).andExpect(jsonPath("$.jugadores.length()").value(n));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 7})
    void crearPartida_rechazaCantidadInvalida(int n) {
        assertFalla(crear(nombres(n)), IllegalArgumentException.class,
                "La partida debe tener entre 2 y 6 jugadores.");
        assertThat(partidaRepository.count()).isZero();
    }

    @Test
    void crearPartida_sinCuerpoDevuelve400() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------ obtener

    @Test
    void obtenerPartida_mapeaTodosLosCampos() throws Exception {
        Long id = sembrar(1, new int[]{1, 2, 3},
                jugador("Ana", new int[]{5}, new int[]{3, 4}),
                jugador("Beto", NADA, new int[]{10}));

        ok(get(BASE + "/" + id))
                .andExpect(jsonPath("$.idPartida").value(id))
                .andExpect(jsonPath("$.estadoPartida").value("EN_PROGRESO"))
                .andExpect(jsonPath("$.cartasRestantesMazo").value(3))
                .andExpect(jsonPath("$.jugadorActualIndex").value(1))
                .andExpect(jsonPath("$.nombreJugadorActual").value("Beto"))
                .andExpect(jsonPath("$.mensajeEvento").value("Estado actual de la partida."))
                .andExpect(jsonPath("$.jugadores[0].nombre").value("Ana"))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante.length()").value(1))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante[0].valor").value(5))
                .andExpect(jsonPath("$.jugadores[0].puntosTotales").value(7))
                .andExpect(jsonPath("$.jugadores[1].puntosTotales").value(10));
    }

    @Test
    void obtenerPartida_inexistenteLanzaError() {
        assertFalla(get(BASE + "/999999"), IllegalArgumentException.class,
                "La partida con ID 999999 no existe.");
    }

    @Test
    void obtenerPartida_idNoNumericoDevuelve400() throws Exception {
        mvc.perform(get(BASE + "/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPartida_sinMazoDevuelveCeroCartas() throws Exception {
        Partida p = construir(EstadoPartida.EN_PROGRESO, 0, NADA, jugador("Ana", NADA, NADA));
        p.setMazo(null);
        Long id = guardar(p);

        ok(get(BASE + "/" + id))
                .andExpect(jsonPath("$.cartasRestantesMazo").value(0))
                .andExpect(jsonPath("$.nombreJugadorActual").value("Ana"));
    }

    @Test
    void obtenerPartida_sinJugadoresNoTieneJugadorActual() throws Exception {
        Long id = sembrar(0, new int[]{1});

        ok(get(BASE + "/" + id))
                .andExpect(jsonPath("$.nombreJugadorActual").doesNotExist())
                .andExpect(jsonPath("$.jugadores.length()").value(0))
                .andExpect(jsonPath("$.cartasRestantesMazo").value(1));
    }

    // ------------------------------------------------------------------ validaciones comunes

    @ParameterizedTest
    @ValueSource(strings = {"guardar", "sacar", "robar?valorCarta=1", "plantarse"})
    void accionesSobrePartidaInexistenteFallan(String accion) {
        assertFalla(post(BASE + "/999999/" + accion), IllegalArgumentException.class,
                "La partida con ID 999999 no existe.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"guardar", "sacar", "robar?valorCarta=1", "plantarse"})
    void accionesSobrePartidaFinalizadaFallan(String accion) {
        Long id = guardar(construir(EstadoPartida.FINALIZADA, 0, new int[]{1},
                jugador("Ana", NADA, NADA), jugador("Beto", NADA, NADA)));
        assertFalla(post(BASE + "/" + id + "/" + accion), IllegalStateException.class, MSG_NO_EN_CURSO);
    }

    @Test
    void accionesSobrePartidaCreadaFallan() {
        Long id = guardar(construir(EstadoPartida.CREADA, 0, new int[]{1},
                jugador("Ana", NADA, NADA), jugador("Beto", NADA, NADA)));
        assertFalla(post(BASE + "/" + id + "/plantarse"), IllegalStateException.class, MSG_NO_EN_CURSO);
    }

    @Test
    void robar_sinParametroValorCartaDevuelve400() throws Exception {
        Long id = sembrar(0, NADA, jugador("Ana", NADA, NADA), jugador("Beto", NADA, NADA));
        mvc.perform(post(BASE + "/" + id + "/robar")).andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------ guardar pelusas

    @Test
    void guardar_muevePelusasAlPozo() throws Exception {
        Long id = sembrar(0, new int[]{1},
                jugador("Ana", new int[]{2, 5}, new int[]{3}),
                jugador("Beto", new int[]{7}, NADA));

        ok(post(BASE + "/" + id + "/guardar"))
                .andExpect(jsonPath("$.mensajeEvento").value("Pelusas guardadas en el pozo seguro."))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante.length()").value(0))
                .andExpect(jsonPath("$.jugadores[0].puntosTotales").value(10))
                .andExpect(jsonPath("$.jugadores[1].cartasDelante.length()").value(1))
                .andExpect(jsonPath("$.jugadores[1].puntosTotales").value(0));

        // persistió
        ok(get(BASE + "/" + id))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante.length()").value(0))
                .andExpect(jsonPath("$.jugadores[0].puntosTotales").value(10));
    }

    @Test
    void guardar_sinCartasDelanteNoCambiaNada() throws Exception {
        Long id = sembrar(0, new int[]{1},
                jugador("Ana", NADA, new int[]{4}), jugador("Beto", NADA, NADA));

        ok(post(BASE + "/" + id + "/guardar"))
                .andExpect(jsonPath("$.jugadores[0].puntosTotales").value(4))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante.length()").value(0));
    }

    @Test
    void guardar_sinJugadoresResponde200() throws Exception {
        Long id = sembrar(0, new int[]{1});
        ok(post(BASE + "/" + id + "/guardar"));
    }

    // ------------------------------------------------------------------ sacar carta

    @Test
    void sacar_agregaLaCartaAlJugadorActual() throws Exception {
        Long id = sembrar(0, new int[]{4, 4, 4},
                jugador("Ana", NADA, NADA), jugador("Beto", NADA, NADA));

        ok(post(BASE + "/" + id + "/sacar"))
                .andExpect(jsonPath("$.mensajeEvento").value("Carta sacada del mazo."))
                .andExpect(jsonPath("$.estadoPartida").value("EN_PROGRESO"))
                .andExpect(jsonPath("$.cartasRestantesMazo").value(2))
                .andExpect(jsonPath("$.jugadorActualIndex").value(0))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante.length()").value(1))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante[0].valor").value(4))
                .andExpect(jsonPath("$.jugadores[1].cartasDelante.length()").value(0));
    }

    @Test
    void sacar_pierdeTurnoConTresCartasYValorRepetido() throws Exception {
        Long id = sembrar(0, new int[]{4, 4},
                jugador("Ana", new int[]{4, 4, 4}, NADA), jugador("Beto", NADA, NADA));

        ok(post(BASE + "/" + id + "/sacar"))
                .andExpect(jsonPath("$.estadoPartida").value("EN_PROGRESO"))
                .andExpect(jsonPath("$.cartasRestantesMazo").value(1))
                .andExpect(jsonPath("$.jugadorActualIndex").value(1))
                .andExpect(jsonPath("$.nombreJugadorActual").value("Beto"))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante.length()").value(0))
                .andExpect(jsonPath("$.jugadores[0].puntosTotales").value(0));
    }

    @Test
    void sacar_conSoloDosCartasYValorRepetidoNoPierdeTurno() throws Exception {
        Long id = sembrar(0, new int[]{4, 4},
                jugador("Ana", new int[]{4, 4}, NADA), jugador("Beto", NADA, NADA));

        ok(post(BASE + "/" + id + "/sacar"))
                .andExpect(jsonPath("$.jugadorActualIndex").value(0))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante.length()").value(3));
    }

    @Test
    void sacar_conTresCartasYValorNuevoNoPierdeTurno() throws Exception {
        Long id = sembrar(0, new int[]{9, 9},
                jugador("Ana", new int[]{1, 2, 3}, NADA), jugador("Beto", NADA, NADA));

        ok(post(BASE + "/" + id + "/sacar"))
                .andExpect(jsonPath("$.jugadorActualIndex").value(0))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante.length()").value(4));
    }

    @Test
    void sacar_ultimaCartaFinalizaYRegistraHistorico() throws Exception {
        Long id = sembrar(0, new int[]{6},
                jugador("Ana", NADA, NADA), jugador("Beto", NADA, new int[]{2}));

        ok(post(BASE + "/" + id + "/sacar"))
                .andExpect(jsonPath("$.estadoPartida").value("FINALIZADA"))
                .andExpect(jsonPath("$.cartasRestantesMazo").value(0))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante.length()").value(0))
                .andExpect(jsonPath("$.jugadores[0].puntosTotales").value(6))
                .andExpect(jsonPath("$.jugadores[1].puntosTotales").value(2));

        PartidaHistorico h = unicoHistorico();
        assertThat(h.getPartidaId()).isEqualTo(String.valueOf(id));
        assertThat(h.getGanadorNombre()).isEqualTo("Ana");
        assertThat(h.getGanadorPuntaje()).isEqualTo(6);
        assertThat(h.getFechaFin()).isNotNull().isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(h.getResumenJugadores()).isEqualTo("Ana (6 pts - Cartas: [6]), Beto (2 pts - Cartas: [2])");
    }

    @Test
    void sacar_conMazoVacioFinalizaYConsolidaCartasDeLaMesa() throws Exception {
        finalizarConMazoVacio(
                jugador("Ana", new int[]{5}, new int[]{1, 9}),
                jugador("Beto", NADA, new int[]{2}));

        PartidaHistorico h = unicoHistorico();
        assertThat(h.getGanadorNombre()).isEqualTo("Ana");
        assertThat(h.getGanadorPuntaje()).isEqualTo(15);
        assertThat(h.getResumenJugadores()).isEqualTo("Ana (15 pts - Cartas: [9, 5, 1]), Beto (2 pts - Cartas: [2])");
    }

    @Test
    void sacar_perderTurnoConLaUltimaCartaFinalizaEnLaSiguienteLlamada() throws Exception {
        Long id = sembrar(0, new int[]{4},
                jugador("Ana", new int[]{4, 4, 4}, NADA), jugador("Beto", NADA, new int[]{1}));

        ok(post(BASE + "/" + id + "/sacar"))
                .andExpect(jsonPath("$.estadoPartida").value("EN_PROGRESO"))
                .andExpect(jsonPath("$.cartasRestantesMazo").value(0))
                .andExpect(jsonPath("$.jugadorActualIndex").value(1));
        assertThat(historicoRepository.count()).isZero();

        ok(post(BASE + "/" + id + "/sacar"))
                .andExpect(jsonPath("$.estadoPartida").value("FINALIZADA"));
        assertThat(historicoRepository.count()).isEqualTo(1);
    }

    @Test
    void sacar_sinJugadoresYMazoVacioTerminaEnEmpateTotal() throws Exception {
        finalizarConMazoVacio();

        PartidaHistorico h = unicoHistorico();
        assertThat(h.getGanadorNombre()).isEqualTo("Empate Total");
        assertThat(h.getGanadorPuntaje()).isZero();
        assertThat(h.getResumenJugadores()).isEmpty();
    }

    // ------------------------------------------------------------------ desempate

    @Test
    void desempate_gananteMasCartasDeValorBajo() throws Exception {
        finalizarConMazoVacio(
                jugador("Ana", NADA, new int[]{5, 5}),
                jugador("Beto", NADA, new int[]{4, 6}));

        PartidaHistorico h = unicoHistorico();
        assertThat(h.getGanadorNombre()).isEqualTo("Beto");
        assertThat(h.getGanadorPuntaje()).isEqualTo(10);
        assertThat(h.getResumenJugadores())
                .isEqualTo("Ana (10 pts - Cartas: [5, 5]), Beto (10 pts - Cartas: [6, 4])");
    }

    @Test
    void desempate_ordenInversoDeJugadores() throws Exception {
        finalizarConMazoVacio(
                jugador("Ana", NADA, new int[]{4, 6}),
                jugador("Beto", NADA, new int[]{5, 5}));

        assertThat(unicoHistorico().getGanadorNombre()).isEqualTo("Ana");
    }

    @Test
    void desempate_decididoEnElValorUno() throws Exception {
        finalizarConMazoVacio(
                jugador("Ana", NADA, new int[]{10}),
                jugador("Beto", NADA, new int[]{1, 9}));

        assertThat(unicoHistorico().getGanadorNombre()).isEqualTo("Beto");
    }

    @Test
    void desempate_empateTotalEligeUnoDeLosJugadores() throws Exception {
        finalizarConMazoVacio(
                jugador("Ana", NADA, new int[]{5}),
                jugador("Beto", NADA, new int[]{5}));

        PartidaHistorico h = unicoHistorico();
        assertThat(h.getGanadorNombre()).isIn("Ana", "Beto");
        assertThat(h.getGanadorPuntaje()).isEqualTo(5);
    }

    // ------------------------------------------------------------------ robar

    @Test
    void robar_tomaCartasDelValorIndicadoDeLosOponentes() throws Exception {
        Long id = sembrar(0, new int[]{1},
                jugador("Ana", NADA, NADA),
                jugador("Beto", new int[]{3, 5, 3}, NADA),
                jugador("Caro", new int[]{3}, NADA));

        ok(post(BASE + "/" + id + "/robar?valorCarta=3"))
                .andExpect(jsonPath("$.mensajeEvento").value("Cartas robadas a rivales."))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante.length()").value(3))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante[0].valor").value(3))
                .andExpect(jsonPath("$.jugadores[1].cartasDelante.length()").value(1))
                .andExpect(jsonPath("$.jugadores[1].cartasDelante[0].valor").value(5))
                .andExpect(jsonPath("$.jugadores[2].cartasDelante.length()").value(0));

        ok(get(BASE + "/" + id))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante.length()").value(3))
                .andExpect(jsonPath("$.jugadores[2].cartasDelante.length()").value(0));
    }

    @Test
    void robar_noTocaLasCartasDelPropioJugador() throws Exception {
        Long id = sembrar(0, new int[]{1},
                jugador("Ana", new int[]{3}, NADA), jugador("Beto", new int[]{3}, NADA));

        ok(post(BASE + "/" + id + "/robar?valorCarta=3"))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante.length()").value(2))
                .andExpect(jsonPath("$.jugadores[1].cartasDelante.length()").value(0));
    }

    @Test
    void robar_sinCoincidenciasNoCambiaNada() throws Exception {
        Long id = sembrar(0, new int[]{1},
                jugador("Ana", NADA, NADA), jugador("Beto", new int[]{3}, NADA));

        ok(post(BASE + "/" + id + "/robar?valorCarta=9"))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante.length()").value(0))
                .andExpect(jsonPath("$.jugadores[1].cartasDelante.length()").value(1));
    }

    // ------------------------------------------------------------------ plantarse

    @Test
    void plantarse_avanzaTurnoYDaLaVuelta() throws Exception {
        Long id = sembrar(0, new int[]{1},
                jugador("Ana", NADA, NADA), jugador("Beto", NADA, NADA));

        ok(post(BASE + "/" + id + "/plantarse"))
                .andExpect(jsonPath("$.mensajeEvento").value("Jugador se ha plantado. Cambiando de turno."))
                .andExpect(jsonPath("$.jugadorActualIndex").value(1))
                .andExpect(jsonPath("$.nombreJugadorActual").value("Beto"));

        ok(post(BASE + "/" + id + "/plantarse"))
                .andExpect(jsonPath("$.jugadorActualIndex").value(0))
                .andExpect(jsonPath("$.nombreJugadorActual").value("Ana"));
    }

    @Test
    void plantarse_desdeElUltimoDeTresVuelveAlPrimero() throws Exception {
        Long id = sembrar(2, new int[]{1},
                jugador("Ana", NADA, NADA), jugador("Beto", NADA, NADA), jugador("Caro", NADA, NADA));

        ok(post(BASE + "/" + id + "/plantarse"))
                .andExpect(jsonPath("$.jugadorActualIndex").value(0));
    }

    @Test
    void plantarse_sinJugadoresMantieneIndice() throws Exception {
        Long id = sembrar(0, new int[]{1});
        ok(post(BASE + "/" + id + "/plantarse")).andExpect(jsonPath("$.jugadorActualIndex").value(0));
    }

    // ------------------------------------------------------------------ histórico

    @Test
    void historico_vacioDevuelveListaVacia() throws Exception {
        ok(get(BASE + "/historico")).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void historico_devuelveRegistrosGuardados() throws Exception {
        historicoRepository.save(new PartidaHistorico("7", LocalDateTime.of(2026, 1, 2, 3, 4, 5),
                "Ana", 42, "Ana (42 pts - Cartas: [10])"));

        ok(get(BASE + "/historico"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].partidaId").value("7"))
                .andExpect(jsonPath("$[0].ganadorNombre").value("Ana"))
                .andExpect(jsonPath("$[0].ganadorPuntaje").value(42))
                .andExpect(jsonPath("$[0].resumenJugadores").value("Ana (42 pts - Cartas: [10])"))
                .andExpect(jsonPath("$[0].fechaFin").exists());
    }
}