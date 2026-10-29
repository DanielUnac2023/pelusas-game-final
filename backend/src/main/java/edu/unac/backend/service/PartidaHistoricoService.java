package edu.unac.backend.service;

import edu.unac.backend.model.PartidaHistorico;
import edu.unac.backend.repository.PartidaHistoricoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartidaHistoricoService {

    private final PartidaHistoricoRepository repository;

    public PartidaHistoricoService(PartidaHistoricoRepository repository) {
        this.repository = repository;
    }

    public List<PartidaHistorico> obtenerHistorico() {
        return repository.findAll();
    }

    public PartidaHistorico guardarHistorico(PartidaHistorico historico) {
        return repository.save(historico);
    }
}