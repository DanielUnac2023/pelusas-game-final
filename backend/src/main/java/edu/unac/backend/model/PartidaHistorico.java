package edu.unac.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "partida_historico")
public class PartidaHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String partidaId;
    private LocalDateTime fechaFin;
    private String ganadorNombre;
    private Integer ganadorPuntaje;

    @Column(length = 1000)
    private String resumenJugadores;

    public PartidaHistorico() {}

    public PartidaHistorico(String partidaId, LocalDateTime fechaFin, String ganadorNombre, Integer ganadorPuntaje, String resumenJugadores) {
        this.partidaId = partidaId;
        this.fechaFin = fechaFin;
        this.ganadorNombre = ganadorNombre;
        this.ganadorPuntaje = ganadorPuntaje;
        this.resumenJugadores = resumenJugadores;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPartidaId() { return partidaId; }
    public void setPartidaId(String partidaId) { this.partidaId = partidaId; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public String getGanadorNombre() { return ganadorNombre; }
    public void setGanadorNombre(String ganadorNombre) { this.ganadorNombre = ganadorNombre; }

    public Integer getGanadorPuntaje() { return ganadorPuntaje; }
    public void setGanadorPuntaje(Integer ganadorPuntaje) { this.ganadorPuntaje = ganadorPuntaje; }

    public String getResumenJugadores() { return resumenJugadores; }
    public void setResumenJugadores(String resumenJugadores) { this.resumenJugadores = resumenJugadores; }
}