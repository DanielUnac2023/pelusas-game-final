package edu.unac.backend.dto;

import edu.unac.backend.model.Carta;
import java.util.List;

public class JugadorDTO {
    private Long id;
    private String nombre;
    private List<Carta> cartasDelante;
    private int puntosTotales;

    public JugadorDTO() {}

    public JugadorDTO(Long id, String nombre, List<Carta> cartasDelante, int puntosTotales) {
        this.id = id;
        this.nombre = nombre;
        this.cartasDelante = cartasDelante;
        this.puntosTotales = puntosTotales;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Carta> getCartasDelante() { return cartasDelante; }
    public void setCartasDelante(List<Carta> cartasDelante) { this.cartasDelante = cartasDelante; }

    public int getPuntosTotales() { return puntosTotales; }
    public void setPuntosTotales(int puntosTotales) { this.puntosTotales = puntosTotales; }
}