package edu.unac.backend.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Mazo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 👈 Clave primaria exigida por JPA para las Entidades

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Carta> cartas = new ArrayList<>();

    public Mazo() {
        inicializarMazo();
        mezclar();
    }

    private void inicializarMazo() {

        // Valores 1 a 5: 13 cartas cada uno (65 cartas en total)
        for (int valor = 1; valor <= 5; valor++) {
            for (int i = 0; i < 13; i++) {
                cartas.add(new Carta(valor));
            }
        }

        // Valores 6 a 10: 9 cartas cada uno (45 cartas en total)
        for (int valor = 6; valor <= 10; valor++) {
            for (int i = 0; i < 9; i++) {
                cartas.add(new Carta(valor));
            }
        }
    }

    public void mezclar() {
        Collections.shuffle(cartas);
    }

    public List<Carta> getCartas() {
        return cartas;
    }

    public int getCantidadRestante() {
        return this.cartas.size();
    }

    public Carta sacarCarta() {
        if (this.cartas.isEmpty()) {
            return null;
        }
        // IMPORTANTE: .remove(0) reduce la lista del mazo de 110 a 109, luego a 108, etc.
        return this.cartas.remove(0);
    }
}