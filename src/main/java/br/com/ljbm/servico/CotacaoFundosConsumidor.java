package br.com.ljbm.servico;

import br.com.ljbm.dto.CotacaoFundoDTO;
import br.com.ljbm.modelo.CotacaoFundo;
import br.com.ljbm.modelo.FundoInvestimento;
import br.com.ljbm.repositorio.CotacaoFundoRepo;
import br.com.ljbm.repositorio.FundoInvestimentoRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Example;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
public class CotacaoFundosConsumidor {
	private final Logger logger = LoggerFactory.getLogger(CotacaoFundosConsumidor.class);

	private final FundoInvestimentoRepo fundoInvestimentoRepo;

	private final CotacaoFundoRepo cotacaoFundoRepo;

    public CotacaoFundosConsumidor(
			CotacaoFundoRepo cotacaoFundoRepo,
			FundoInvestimentoRepo fundoInvestimentoRepo) {

		this.cotacaoFundoRepo = cotacaoFundoRepo;
		this.fundoInvestimentoRepo = fundoInvestimentoRepo;
	}

	@KafkaListener(id = "cotacaoFundosGroup", topics = "cotacoes-fundos")
	public void listen(CotacaoFundoDTO cf) {
		logger.info("{} recebida", cf);
		var filtro = new FundoInvestimento();
		filtro.setNome(cf.nomeFundo());
        for (FundoInvestimento fi : fundoInvestimentoRepo.findAll(Example.of(filtro))) {
            logger.info("atualizando cotação do {}", fi);
            var c = new CotacaoFundo(cf.dataCotacao(), cf.valorCota(), fi);
            try {
				CotacaoFundo cMerged = cotacaoFundoRepo.mergePorDataFundo(c);
                logger.info("{} sincronizada.", cMerged);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }

}