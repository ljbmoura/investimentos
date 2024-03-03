package br.com.ljbm.repositorio;

import br.com.ljbm.modelo.FundoInvestimento;
import br.com.ljbm.servico.CotacaoFundosIntegracao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.ljbm.modelo.CotacaoFundo;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface CotacaoFundoRepo extends JpaRepository<CotacaoFundo, Long>
{
    Logger logger = LoggerFactory.getLogger(CotacaoFundosIntegracao.class);
    default CotacaoFundo mergePorDataFundo (CotacaoFundo cf) {
        var fi = new FundoInvestimento();
        fi.setIde(cf.getFundoInvestimento().getIde());
        Example<CotacaoFundo> e = Example.of(new CotacaoFundo(cf.getDataCotacao(), null, fi));
        Optional<CotacaoFundo> _cfAtual = this.findOne(e);
        CotacaoFundo cfPersistido;
        if (_cfAtual.isPresent()) {
            var cfAtual = _cfAtual.get();
            cfAtual.setValorCota(cf.getValorCota());
            cfPersistido = this.save(cfAtual);
        } else {
            cfPersistido = this.save(cf);
        }
//        this.flush();
//        logger.info("cotação {} {} {} sincronizada.", cfPersistido.getIde(), cfPersistido.getDataCotacao(), cfPersistido.getValorCota());
//        var cfPersistido2 = this.getReferenceById(cfPersistido.getIde());
        logger.info("{} sincronizada.", cfPersistido);
        return cfPersistido;
    }
}

