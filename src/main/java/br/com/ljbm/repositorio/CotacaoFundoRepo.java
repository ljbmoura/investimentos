package br.com.ljbm.repositorio;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.ljbm.modelo.CotacaoFundo;

@Repository
public interface CotacaoFundoRepo extends JpaRepository<CotacaoFundo, Long>
{
    default CotacaoFundo mergePorDataFundo (CotacaoFundo cf) {
        Example<CotacaoFundo> e = Example.of(new CotacaoFundo(cf.getDataCotacao(), null, cf.getFundoInvestimento()));
        return this.findOne(e).orElseGet(() -> this.save(cf));
    }
}

