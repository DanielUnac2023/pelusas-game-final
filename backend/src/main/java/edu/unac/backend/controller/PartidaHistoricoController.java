package edu.unac.backend.controller;

import edu.unac.backend.model.PartidaHistorico;
import edu.unac.backend.service.PartidaHistoricoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partidas")
@CrossOrigin(origins = "*") // Permite la conexión desde el frontend en Vue
public class PartidaHistoricoController {

    private final PartidaHistoricoService service;

    public PartidaHistoricoController(PartidaHistoricoService service) {
        this.service = service;
    }

    @GetMapping("/historico")
    public ResponseEntity<List<PartidaHistorico>> obtenerHistorico() {
        List<PartidaHistorico> historico = service.obtenerHistorico();
        return ResponseEntity.ok(historico);
    }
}