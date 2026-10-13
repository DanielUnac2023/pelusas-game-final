package edu.unac.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Carta> cartasDelante = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Carta> pozoPuntos = new ArrayList<>();

    public Jugador() {
    }

    public Jugador(String nombre) {
        this.nombre = nombre;
    }

    public Jugador(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Metodo de utilidad para calcular los puntos acumulados en el pozo seguro
    public int getPuntuacionTotal() {
        return pozoPuntos.stream()
                .mapToInt(Carta::getValor)
                .sum();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Carta> getCartasDelante() {
        return cartasDelante;
    }

    public void setCartasDelante(List<Carta> cartasDelante) {
        this.cartasDelante = cartasDelante;
    }

    public List<Carta> getPozoPuntos() {
        return pozoPuntos;
    }

    public void setPozoPuntos(List<Carta> pozoPuntos) {
        this.pozoPuntos = pozoPuntos;
    }
}