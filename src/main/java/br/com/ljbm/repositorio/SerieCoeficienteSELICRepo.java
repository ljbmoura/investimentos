package br.com.ljbm.repositorio;

import br.com.ljbm.modelo.SerieCoeficienteSELIC;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

;

@Repository
public interface SerieCoeficienteSELICRepo extends JpaRepository<SerieCoeficienteSELIC, Long> {

    default public Boolean existeCoeficienteSELIC(LocalDate dataCompra, LocalDate dataAlvo) {

        var filtro = new SerieCoeficienteSELIC();
        filtro.setDataInicio(dataCompra);
        filtro.setDataFim(dataAlvo);
        return this.exists(Example.of(filtro));
    }
}

