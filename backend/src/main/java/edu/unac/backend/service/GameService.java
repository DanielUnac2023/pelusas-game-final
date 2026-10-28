package edu.unac.backend.service;

import edu.unac.backend.model.Carta;
import edu.unac.backend.model.EstadoPartida;
import edu.unac.backend.model.Jugador;
import edu.unac.backend.model.Partida;
import edu.unac.backend.repository.PartidaRepository;
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
    public Partida sacarCarta(Long idPartida) {
        Partida partida = obtenerPartida(idPartida);
        validarPartidaEnProgreso(partida);

        Jugador actual = partida.getJugadorActual();
        Carta cartaSacada = partida.getMazo().sacarCarta();

        if (cartaSacada == null) {
            finalizarPartida(partida);
            return partidaRepository.save(partida);
        }

        List<Carta> cartasMesa = actual.getCartasDelante();

        // Verificar si la carta ya existía en la mesa del jugador actual
        boolean yaTieneValor = cartasMesa.stream()
                .anyMatch(c -> c.getValor() == cartaSacada.getValor());

        // Regla de Pérdida de Turno: >= 3 cartas en mesa Y valor repetido
        if (cartasMesa.size() >= 3 && yaTieneValor) {
            // Pierde el turno y se descartan todas las cartas de la mesa
            cartasMesa.clear();
            partida.siguienteTurno();
        } else {
            // Se agrega la carta a la mesa del jugador
            cartasMesa.add(cartaSacada);
        }

        // Si el mazo se agotó al sacar esta carta, se finaliza el juego
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