package edu.unac.backend.service;

import edu.unac.backend.model.*;
import edu.unac.backend.repository.PartidaHistoricoRepository;
import edu.unac.backend.repository.PartidaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.ArrayList;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService {

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private PartidaHistoricoRepository partidaHistoricoRepository;

    private static final int NOMBRE_MIN = 3;
    private static final int NOMBRE_MAX = 15;
    /**
     * Crea e inicializa una nueva partida de 2 a 6 jugadores.
     */
    public Partida crearPartida(List<String> nombresJugadores) {
        if (nombresJugadores == null || nombresJugadores.size() < 2 || nombresJugadores.size() > 6) {
            throw new IllegalArgumentException("La partida debe tener entre 2 y 6 jugadores.");
        }

        Partida partida = new Partida();
        partida.setEstado(EstadoPartida.EN_PROGRESO);

        List<Jugador> jugadores = new ArrayList<>();
        for (String nombre : nombresJugadores) {
            Jugador j = new Jugador();
            j.setNombre(validarNombre(nombre));
            jugadores.add(j);
        }
        partida.setJugadores(jugadores);
        partida.getMazo().mezclar();

        return partidaRepository.save(partida);
    }

    /**
     * Paso 1 — Guardar Pelusas:
     * Traslada las cartas acumuladas de turnos anteriores (cartasDelante)
     * al pozo seguro de puntos (pozoPuntos).
     */
    public Partida guardarPelusas(Long idPartida) {
        Partida partida = obtenerPartida(idPartida);
        validarPartidaEnProgreso(partida);

        if (partida.getValorUltimaCarta() != null) {
            throw new IllegalStateException("Solo puedes guardar al inicio del turno, antes de sacar una carta.");
        }

        Jugador actual = partida.getJugadorActual();
        if (actual.getCartasDelante().isEmpty()) {
            throw new IllegalStateException("No tienes cartas delante para guardar.");
        }

        actual.getPozoPuntos().addAll(actual.getCartasDelante());
        actual.getCartasDelante().clear();
        return partidaRepository.save(partida);
    }

    /**
     * Paso 2 — Sacar Carta del Mazo:
     * Evalúa si aplica la Regla de Pérdida de Turno.
     */
    @Transactional
    public Partida sacarCarta(Long id) {
        Partida partida = obtenerPartida(id);
        validarPartidaEnProgreso(partida);

        // 1. Extraer carta del mazo de forma aleatoria
        Carta cartaSacada = partida.getMazo().sacarCarta();

        // Si el mazo se vació, finaliza la partida
        if (cartaSacada == null) {
            finalizarPartida(partida);
            return partidaRepository.save(partida);
        }

        Jugador actual = partida.getJugadorActual();

        // 2. Verificar si el valor de la nueva carta YA EXISTÍA en las cartas delante
        // del jugador
        boolean yaTeniaEseValor = actual.getCartasDelante().stream()
                .anyMatch(c -> c.getValor() == cartaSacada.getValor());

        // 3. Contar cuántas cartas tenía delante ANTES de añadir la nueva
        int cantidadCartasAntes = actual.getCartasDelante().size();

        // 4. Evaluar la Regla Oficial de Pérdida
        // Condición: ¿Tenía 3 o más cartas en total Y la nueva carta repite un valor
        // existente?
        if (cantidadCartasAntes >= 3 && yaTeniaEseValor) {
            // PERDIÓ EL TURNO
            actual.getCartasDelante().clear(); // Se descartan todas las cartas de la mesa
            partida.siguienteTurno(); // Pasa al siguiente jugador
            return partidaRepository.save(partida);
        }

        // 5. Si no perdió (tenía < 3 cartas o el valor era nuevo), se agrega la carta
        // normalmente
        Carta nuevaCarta = new Carta(cartaSacada.getValor());
        actual.getCartasDelante().add(nuevaCarta);
        partida.setValorUltimaCarta(cartaSacada.getValor());

        // Si tras haber sacado la carta actual el mazo quedó con 0 cartas, finaliza el
        // juego
        if (partida.getMazo().getCantidadRestante() == 0) {
            finalizarPartida(partida);
        }
        return partidaRepository.save(partida);
    }

    /**
     * Paso 3 — Robar Cartas:
     * Toma las cartas de igual valor recién sacado que tengan los oponentes.
     */
    @Transactional
    public Partida robarCartas(Long idPartida) {
        Partida partida = obtenerPartida(idPartida);
        validarPartidaEnProgreso(partida);

        Integer valorUltimo = partida.getValorUltimaCarta();
        if (valorUltimo == null) {
            throw new IllegalStateException("Debes sacar una carta primero para poder robar.");
        }

        Jugador actual = partida.getJugadorActual();
        if (actual == null) {
            throw new IllegalStateException("No se pudo identificar al jugador actual.");
        }

        boolean seRoboAlguna = false;

        for (Jugador oponente : partida.getJugadores()) {
            if (oponente.getId().equals(actual.getId())) continue;

            Iterator<Carta> it = oponente.getCartasDelante().iterator();
            while (it.hasNext()) {
                Carta carta = it.next();
                if (carta.getValor() == valorUltimo.intValue()) {
                    it.remove();
                    actual.getCartasDelante().add(carta);
                    seRoboAlguna = true;
                }
            }
        }

        if (!seRoboAlguna) {
            throw new IllegalStateException("No hay cartas de valor " + valorUltimo + " para robar a ningún oponente.");
        }
        return partidaRepository.save(partida);
    }
    /**
     * Finaliza voluntariamente el turno y le da paso al siguiente jugador.
     */
    public Partida plantarse(Long idPartida) {
        Partida partida = obtenerPartida(idPartida);
        validarPartidaEnProgreso(partida);

        partida.siguienteTurno();
        return partidaRepository.save(partida);
    }

    /**
     * Finalización automática de la partida al agotarse el mazo.
     * Consolida puntos y define al ganador o desempate.
     */
    private void finalizarPartida(Partida partida) {
        partida.setEstado(EstadoPartida.FINALIZADA);

        // Guardar automáticamente cualquier carta remanente en mesa
        for (Jugador j : partida.getJugadores()) {
            j.getPozoPuntos().addAll(j.getCartasDelante());
            j.getCartasDelante().clear();
        }

        // 2. Determinar el ganador aplicando la regla de desempate
        Jugador ganador = resolverGanadorConDesempate(partida.getJugadores());

        // 3. Generar el resumen de jugadores con sus puntos Y SUS CARTAS
        StringBuilder resumen = new StringBuilder();
        for (int i = 0; i < partida.getJugadores().size(); i++) {
            Jugador j = partida.getJugadores().get(i);
            int puntos = j.getPozoPuntos().stream().mapToInt(Carta::getValor).sum();

            // Extraer y ordenar la lista de valores de cartas (ej: [8, 5, 4, 2])
            List<Integer> valoresCartas = j.getPozoPuntos().stream()
                    .map(Carta::getValor)
                    .sorted(Comparator.reverseOrder())
                    .toList();

            resumen.append(j.getNombre())
                    .append(" (").append(puntos).append(" pts - Cartas: ").append(valoresCartas).append(")");

            if (i < partida.getJugadores().size() - 1) {
                resumen.append(", ");
            }
        }

        // 4. Crear y guardar el registro del histórico
        PartidaHistorico historico = new PartidaHistorico();
        historico.setPartidaId(String.valueOf(partida.getId()));
        historico.setFechaFin(LocalDateTime.now());
        historico.setGanadorNombre(ganador != null ? ganador.getNombre() : "Empate Total");
        historico.setGanadorPuntaje(ganador != null ? calcularPuntos(ganador) : 0);
        historico.setResumenJugadores(resumen.toString());

        partidaHistoricoRepository.save(historico);
    }

    public Partida obtenerPartida(Long idPartida) {
        return partidaRepository.findById(idPartida)
                .orElseThrow(() -> new IllegalArgumentException("La partida con ID " + idPartida + " no existe."));
    }

    private void validarPartidaEnProgreso(Partida partida) {
        if (partida.getEstado() != EstadoPartida.EN_PROGRESO) {
            throw new IllegalStateException("La partida no está en curso.");
        }
    }

    private String validarNombre(String nombre) {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre del jugador no puede ser nulo.");
        }
        String limpio = nombre.trim();
        if (limpio.length() < NOMBRE_MIN) {
            throw new IllegalArgumentException(
                    "El nombre '" + limpio + "' tiene menos de 3 caracteres.");
        }
        if (limpio.length() > NOMBRE_MAX) {
            throw new IllegalArgumentException(
                    "El nombre '" + limpio + "' tiene más de 15 caracteres.");
        }
        if (NombreProhibido.contiene(limpio)) {
            throw new IllegalArgumentException(
                    "El nombre '" + limpio + "' no está permitido.");
        }
        return limpio;
    }

    public List<PartidaHistorico> obtenerHistorial() {
        return partidaHistoricoRepository.findAll();
    }

    /**
     * Mecanismo de Desempate (Segun Especificacion Oficial):
     * 1. Compara por puntaje total acumulado.
     * 2. Si hay empate, compara secuencialmente la cantidad de cartas de valor 1, 2, 3... hasta 10.
     */
    private Jugador resolverGanadorConDesempate(List<Jugador> jugadores) {
        if (jugadores.isEmpty()) {
            return null;
        }

        return jugadores.stream().max((j1, j2) -> {
            int puntos1 = calcularPuntos(j1);
            int puntos2 = calcularPuntos(j2);

            // Criterio 1: Mayor puntaje acumulado
            if (puntos1 != puntos2) {
                return Integer.compare(puntos1, puntos2);
            }

            // Criterio 2: Desempate por cantidad de cartas de valor 1 al 10
            for (int valor = 1; valor <= 10; valor++) {
                final int val = valor;
                long cant1 = j1.getPozoPuntos().stream().filter(c -> c.getValor() == val).count();
                long cant2 = j2.getPozoPuntos().stream().filter(c -> c.getValor() == val).count();

                if (cant1 != cant2) {
                    return Long.compare(cant1, cant2);
                }
            }

            return 0; // Empate total en cantidad de cartas en todos los valores
        }).orElse(null);
    }

    private int calcularPuntos(Jugador jugador) {
        return jugador.getPozoPuntos().stream().mapToInt(Carta::getValor).sum();
    }
}