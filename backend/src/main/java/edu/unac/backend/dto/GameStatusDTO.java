package edu.unac.backend.dto;

import edu.unac.backend.model.EstadoPartida;
import java.util.List;

public class GameStatusDTO {
    private Long idPartida;
    private EstadoPartida estadoPartida;
    private int cartasRestantesMazo;
    private int jugadorActualIndex;
    private String nombreJugadorActual;
    private String mensajeEvento;
    private List<String> accionesPermitidas;
    private List<JugadorDTO> jugadores;

    public GameStatusDTO() {}

    // Getters y Setters
    public Long getIdPartida() { return idPartida; }
    public void setIdPartida(Long idPartida) { this.idPartida = idPartida; }

    public EstadoPartida getEstadoPartida() { return estadoPartida; }
    public void setEstadoPartida(EstadoPartida estadoPartida) { this.estadoPartida = estadoPartida; }

    public int getCartasRestantesMazo() { return cartasRestantesMazo; }
    public void setCartasRestantesMazo(int cartasRestantesMazo) { this.cartasRestantesMazo = cartasRestantesMazo; }

    public int getJugadorActualIndex() { return jugadorActualIndex; }
    public void setJugadorActualIndex(int jugadorActualIndex) { this.jugadorActualIndex = jugadorActualIndex; }

    public String getNombreJugadorActual() { return nombreJugadorActual; }
    public void setNombreJugadorActual(String nombreJugadorActual) { this.nombreJugadorActual = nombreJugadorActual; }

    public String getMensajeEvento() { return mensajeEvento; }
    public void setMensajeEvento(String mensajeEvento) { this.mensajeEvento = mensajeEvento; }

    public List<String> getAccionesPermitidas() { return accionesPermitidas; }
    public void setAccionesPermitidas(List<String> accionesPermitidas) { this.accionesPermitidas = accionesPermitidas; }

    public List<JugadorDTO> getJugadores() { return jugadores; }
    public void setJugadores(List<JugadorDTO> jugadores) { this.jugadores = jugadores; }
}