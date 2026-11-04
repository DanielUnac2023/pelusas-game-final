package edu.unac.backend.controller;

import edu.unac.backend.dto.GameStatusDTO;
import edu.unac.backend.dto.JugadorDTO;
import edu.unac.backend.model.Jugador;
import edu.unac.backend.model.Partida;
import edu.unac.backend.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.unac.backend.model.PartidaHistorico;
import edu.unac.backend.repository.PartidaHistoricoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/partidas")
@CrossOrigin(origins = "*") // Permite la comunicación fluida con el frontend en Vue 3
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping
    public ResponseEntity<GameStatusDTO> crearPartida(@RequestBody List<String> nombresJugadores) {
        try {
            Partida partida = gameService.crearPartida(nombresJugadores);
            return ResponseEntity.ok(convertirADTO(partida, "Partida creada exitosamente."));
        } catch (IllegalArgumentException e) {
            GameStatusDTO error = new GameStatusDTO();
            error.setMensajeEvento(e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameStatusDTO> obtenerPartida(@PathVariable Long id) {
        Partida partida = gameService.obtenerPartida(id);
        return ResponseEntity.ok(convertirADTO(partida, "Estado actual de la partida."));
    }

    @PostMapping("/{id}/guardar")
    public ResponseEntity<GameStatusDTO> guardarPelusas(@PathVariable Long id) {
        try {
            Partida partida = gameService.guardarPelusas(id);
            return ResponseEntity.ok(convertirADTO(partida, "Pelusas guardadas en el pozo seguro."));
        } catch (IllegalStateException e) {
            Partida partidaActual = gameService.obtenerPartida(id);
            return ResponseEntity.badRequest().body(convertirADTO(partidaActual, e.getMessage()));
        }
    }

    @PostMapping("/{id}/sacar")
    public ResponseEntity<GameStatusDTO> sacarCarta(@PathVariable Long id) {
        Partida partida = gameService.sacarCarta(id);
        return ResponseEntity.ok(convertirADTO(partida, "Carta sacada del mazo."));
    }

    @PostMapping("/{id}/robar")
    public ResponseEntity<GameStatusDTO> robarCartas(@PathVariable Long id) {
        try {
            Partida partida = gameService.robarCartas(id);
            return ResponseEntity.ok(convertirADTO(partida, "Cartas robadas a rivales con éxito."));
        } catch (IllegalStateException e) {
            // e.getMessage() toma exactamente "Debes sacar una carta primero para poder robar."
            // o "No hay cartas de valor X para robar a ningún oponente." según la función original.
            Partida partidaActual = gameService.obtenerPartida(id);
            return ResponseEntity.badRequest().body(convertirADTO(partidaActual, e.getMessage()));
        }
    }

    @PostMapping("/{id}/plantarse")
    public ResponseEntity<GameStatusDTO> plantarse(@PathVariable Long id) {
        Partida partida = gameService.obtenerPartida(id);
        Jugador jugadorActual = partida.getJugadorActual();

        // Validar que el jugador haya sacado al menos una carta en este turno
        boolean haSacadoAlMenosUnaCarta = !jugadorActual.getCartasDelante().isEmpty();

        if (!haSacadoAlMenosUnaCarta) {
            return ResponseEntity.badRequest()
                    .body(convertirADTO(partida, "Debe sacar al menos una carta antes de poder plantarse."));
        }

        Partida partidaActualizada = gameService.plantarse(id);
        return ResponseEntity.ok(convertirADTO(partidaActualizada, "Jugador se ha plantado. Cambiando de turno."));
    }

    /**
     * Mapeador helper de la Entidad Partida al GameStatusDTO que consume el
     * frontend.
     */
    private GameStatusDTO convertirADTO(Partida partida, String mensaje) {
        GameStatusDTO dto = new GameStatusDTO();
        dto.setIdPartida(partida.getId());
        dto.setEstadoPartida(partida.getEstado());

        if (partida.getMazo() != null) {
            dto.setCartasRestantesMazo(partida.getMazo().getCantidadRestante());
        }

        dto.setJugadorActualIndex(partida.getIndiceJugadorActual());

        Jugador actual = partida.getJugadorActual();
        if (actual != null) {
            dto.setNombreJugadorActual(actual.getNombre());
        }

        dto.setMensajeEvento(mensaje);

        // Definición de acciones permitidas para el cliente delgado
        List<String> acciones = new ArrayList<>();
        acciones.add("SACAR_CARTA");
        acciones.add("PLANTARSE");
        acciones.add("GUARDAR_PELUSAS");
        acciones.add("ROBAR");
        dto.setAccionesPermitidas(acciones);

        // Mapeo de la lista de jugadores a JugadorDTO
        List<JugadorDTO> jugadorDTOs = partida.getJugadores().stream().map(j -> {
            int puntos = j.getPozoPuntos().stream().mapToInt(c -> c.getValor()).sum();
            return new JugadorDTO(j.getId(), j.getNombre(), j.getCartasDelante(), puntos);
        }).collect(Collectors.toList());

        dto.setJugadores(jugadorDTOs);

        return dto;
    }

    @GetMapping("/historico")
    public ResponseEntity<List<PartidaHistorico>> obtenerHistorial() {
        List<PartidaHistorico> historial = gameService.obtenerHistorial();
        return ResponseEntity.ok(historial);
    }
}