package br.com.ljbm.repositorio;

import br.com.ljbm.modelo.SerieCoeficienteSELIC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SerieCoeficienteSELICRepo extends JpaRepository<SerieCoeficienteSELIC, Long> { }

