package edu.unac.backend.service;

import edu.unac.backend.model.Carta;
import edu.unac.backend.model.EstadoPartida;
import edu.unac.backend.model.Jugador;
import edu.unac.backend.model.Partida;
import edu.unac.backend.repository.PartidaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {

    @Autowired
    private PartidaRepository partidaRepository;

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
            j.setNombre(nombre);
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

        Jugador actual = partida.getJugadorActual();
        if (actual != null && !actual.getCartasDelante().isEmpty()) {
            actual.getPozoPuntos().addAll(actual.getCartasDelante());
            actual.getCartasDelante().clear();
        }

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

        // 2. Verificar si el valor de la nueva carta YA EXISTÍA en las cartas delante del jugador
        boolean yaTeniaEseValor = actual.getCartasDelante().stream()
                .anyMatch(c -> c.getValor() == cartaSacada.getValor());

        // 3. Contar cuántas cartas tenía delante ANTES de añadir la nueva
        int cantidadCartasAntes = actual.getCartasDelante().size();

        // 4. Evaluar la Regla Oficial de Pérdida
        // Condición: ¿Tenía 3 o más cartas en total Y la nueva carta repite un valor existente?
        if (cantidadCartasAntes >= 3 && yaTeniaEseValor) {
            // PERDIÓ EL TURNO
            actual.getCartasDelante().clear(); // Se descartan todas las cartas de la mesa
            partida.siguienteTurno(); // Pasa al siguiente jugador
            return partidaRepository.save(partida);
        }

        // 5. Si no perdió (tenía < 3 cartas o el valor era nuevo), se agrega la carta normalmente
        Carta nuevaCarta = new Carta(cartaSacada.getValor());
        actual.getCartasDelante().add(nuevaCarta);

        // Si tras haber sacado la carta actual el mazo quedó con 0 cartas, finaliza el juego
        if (partida.getMazo().getCantidadRestante() == 0) {
            finalizarPartida(partida);
        }

        return partidaRepository.save(partida);
    }

    /**
     * Paso 3 — Robar Cartas:
     * Toma las cartas de igual valor recién sacado que tengan los oponentes.
     */
    public Partida robarCartas(Long idPartida, int valorCarta) {
        Partida partida = obtenerPartida(idPartida);
        validarPartidaEnProgreso(partida);

        Jugador actual = partida.getJugadorActual();

        for (Jugador oponente : partida.getJugadores()) {
            if (!oponente.equals(actual)) {
                Iterator<Carta> iterator = oponente.getCartasDelante().iterator();
                while (iterator.hasNext()) {
                    Carta c = iterator.next();
                    if (c.getValor() == valorCarta) {
                        actual.getCartasDelante().add(c);
                        iterator.remove();
                    }
                }
            }
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
}