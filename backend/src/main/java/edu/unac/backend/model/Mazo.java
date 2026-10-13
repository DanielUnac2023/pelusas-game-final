package edu.unac.backend.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mazo {

    private final List<Carta> cartas = new ArrayList<>();

    public Mazo() {
        inicializarMazo();
    }

    private void inicializarMazo() {
        long idCounter = 1;

        // Valores 1 a 5: 13 cartas cada uno (65 cartas en total)
        for (int valor = 1; valor <= 5; valor++) {
            for (int i = 0; i < 13; i++) {
                cartas.add(new Carta(idCounter++, valor));
            }
        }

        // Valores 6 a 10: 9 cartas cada uno (45 cartas en total)
        for (int valor = 6; valor <= 10; valor++) {
            for (int i = 0; i < 9; i++) {
                cartas.add(new Carta(idCounter++, valor));
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
        return cartas.size();
    }

    public Carta sacarCarta() {
        if (cartas.isEmpty()) {
            return null;
        }
        return cartas.remove(0);
    }
}