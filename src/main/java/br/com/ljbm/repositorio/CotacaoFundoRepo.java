package br.com.ljbm.repositorio;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.ljbm.modelo.CotacaoFundo;

import java.util.Optional;
@Repository
public interface CotacaoFundoRepo extends JpaRepository<CotacaoFundo, Long>
{
//    @Transactional
    default CotacaoFundo mergePorDataFundo (CotacaoFundo cf) {
        var filtro = new CotacaoFundo();
        filtro.setDataCotacao(cf.getDataCotacao());
        filtro.setIdeFundoInvestimento(cf.getFundoInvestimento().getIde());
        Optional<CotacaoFundo> _cfAtual = this.findOne(Example.of(filtro));

        final CotacaoFundo cfsincronizado;
        if (_cfAtual.isPresent()) {
            cfsincronizado = _cfAtual.get();
            cfsincronizado.setValorCota(cf.getValorCota());
        } else {
            cfsincronizado = save(cf);
        }
        return cfsincronizado;
    }
}

