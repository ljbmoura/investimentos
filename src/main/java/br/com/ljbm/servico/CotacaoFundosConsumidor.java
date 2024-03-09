package br.com.ljbm.servico;

import br.com.ljbm.dto.CotacaoFundoDTO;
import br.com.ljbm.modelo.CotacaoFundo;
import br.com.ljbm.repositorio.CotacaoFundoRepo;
import br.com.ljbm.repositorio.FundoInvestimentoRepo;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class CotacaoFundosConsumidor {
	private static final Logger logger = LoggerFactory.getLogger(CotacaoFundosConsumidor.class);

	private final FundoInvestimentoRepo fundoInvestimentoRepo;

	private final CotacaoFundoRepo cotacaoFundoRepo;

	ExecutorService executorService = Executors.newFixedThreadPool(30);

    public CotacaoFundosConsumidor(
			CotacaoFundoRepo cotacaoFundoRepo,
			FundoInvestimentoRepo fundoInvestimentoRepo) {

		this.cotacaoFundoRepo = cotacaoFundoRepo;
		this.fundoInvestimentoRepo = fundoInvestimentoRepo;
	}

	@KafkaListener (
			id = "CF-Group",
			topics = "cotacoes-fundos",
			groupId = "CF",
			concurrency = "3") // pois o tópico foi criado com 3 partições
	@Transactional
	public void atualizacaoCotacaoFundoListen (
		CotacaoFundoDTO cfDTO,
		@Header(KafkaHeaders.RECEIVED_PARTITION) int particao,
//		@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
//		@Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts)
		@Header(KafkaHeaders.RECEIVED_KEY) String chaveFundoInvestimento) {
		executorService.submit( () -> {
			logger.debug("k={} v={} recebida da partição {}", chaveFundoInvestimento, cfDTO, particao);
			try {
				var cf = new CotacaoFundo(cfDTO.dataCotacao(), cfDTO.valorCota(),
						fundoInvestimentoRepo.getReferenceById(Long.valueOf(chaveFundoInvestimento)));
				CotacaoFundo cMerged = cotacaoFundoRepo.mergePorDataFundo(cf);
				logger.info("sincronizada {}.", cMerged);
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getLocalizedMessage());
			}
		});
	}

	@KafkaListener (
			id = "SELIC-Group",
			topics = "cotacoes-fundos",
			groupId = "SELIC",
			concurrency = "3") // pois o tópico foi criado com 3 partições
	@Transactional
	public void atualizacaoCoeficientesSerieSELIClisten (
			CotacaoFundoDTO cfDTO,
			@Header(KafkaHeaders.RECEIVED_PARTITION) int particao,
//		@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
//		@Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts)
			@Header(KafkaHeaders.RECEIVED_KEY) String chaveFundoInvestimento) {
//		executorService.submit( () -> {
			logger.debug("k={} v={} recebida da partição {}", chaveFundoInvestimento, cfDTO, particao);

//		List<PosicaoTituloPorAgente> extrato = new ArrayList<PosicaoTituloPorAgente>();
		LocalDate dataRefAux = cfDTO.dataCotacao();
//		List<FundoInvestimento> fundos = servicoFPDominio.getAllFundoInvestimento();
//		fundos.stream().forEach(fundo -> {
//			posicao.setCompras(fundo.getAplicacoes().stream().filter(a -> ! a.getDataCompra().isAfter(dataRefAux) && a.getSaldoCotas().compareTo(BigDecimal.ZERO) > 0)
//					.collect(Collectors.toList()));
//			if (posicao.getCompras().size() > 0) {
//				extrato.add(posicao);
//			}
//		});

//		});
	}

}