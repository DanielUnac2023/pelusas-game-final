package edu.unac.backend.repository;

import edu.unac.backend.model.PartidaHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartidaHistoricoRepository extends JpaRepository<PartidaHistorico, Long> {
}