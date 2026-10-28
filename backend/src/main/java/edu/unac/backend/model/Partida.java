package edu.unac.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EstadoPartida estado = EstadoPartida.CREADA;

    private int indiceJugadorActual = 0;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Jugador> jugadores = new ArrayList<>();

    // ✅ OBLIGATORIO: CascadeType.ALL para que JPA guarde los cambios internos del mazo
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Mazo mazo = new Mazo();

    public Partida() {
        this.mazo = new Mazo();
    }

    public Partida(Long id) {
        this.id = id;
        this.mazo = new Mazo();
    }

    // Avanza al siguiente turno cíclicamente
    public void siguienteTurno() {
        if (!jugadores.isEmpty()) {
            this.indiceJugadorActual = (this.indiceJugadorActual + 1) % jugadores.size();
        }
    }

    public Jugador getJugadorActual() {
        if (jugadores.isEmpty()) {
            return null;
        }
        return jugadores.get(indiceJugadorActual);
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartida estado) {
        this.estado = estado;
    }

    public int getIndiceJugadorActual() {
        return indiceJugadorActual;
    }

    public void setIndiceJugadorActual(int indiceJugadorActual) {
        this.indiceJugadorActual = indiceJugadorActual;
    }

    public List<Jugador> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<Jugador> jugadores) {
        this.jugadores = jugadores;
    }

    public Mazo getMazo() {
        return mazo;
    }

    public void setMazo(Mazo mazo) {
        this.mazo = mazo;
    }
}