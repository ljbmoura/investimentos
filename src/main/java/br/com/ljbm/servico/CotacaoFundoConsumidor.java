package br.com.ljbm.servico;

import br.com.ljbm.dto.CotacaoFundoDTO;
import br.com.ljbm.modelo.CotacaoFundo;
import br.com.ljbm.repositorio.CotacaoFundoRepo;
import br.com.ljbm.repositorio.FundoInvestimentoRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Service
@RequiredArgsConstructor
public class CotacaoFundoConsumidor {

	private final FundoInvestimentoRepo fundoInvestimentoRepo;

	private final CotacaoFundoRepo cotacaoFundoRepo;

	ExecutorService executorService = Executors.newFixedThreadPool(30);

	@KafkaListener (
			id = "CF-Group",
			topics = "cotacoes-fundos",
			groupId = "CF",
			concurrency = "3") // pois o tópico foi criado com 3 partições
	@Transactional
	public void atualizacaoCotacaoFundoListen (ConsumerRecord<String, CotacaoFundoDTO> mensagem) {
		executorService.submit( () -> {
			var cfDTO = mensagem.value();
			var chaveFundoInvestimento = mensagem.key();
			var particao = mensagem.topic();
			log.debug("k={} v={} recebida da partição {}", chaveFundoInvestimento, cfDTO, particao);
			try {
				var cf = new CotacaoFundo(cfDTO.dataCotacao(), cfDTO.valorCota(),
						fundoInvestimentoRepo.getReferenceById(Long.valueOf(chaveFundoInvestimento)));
				CotacaoFundo cMerged = cotacaoFundoRepo.mergePorDataFundo(cf);
				log.info("sincronizada {}.", cMerged);

			} catch (DataIntegrityViolationException e) {
				log.error(e.getLocalizedMessage());
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
			ConsumerRecord<String, CotacaoFundoDTO> mensagem
	) {
//		executorService.submit( () -> {
			log.info("k={} v={} recebida da partição {}", mensagem.key(), mensagem.value(), mensagem.topic());

//		List<PosicaoTituloPorAgente> extrato = new ArrayList<PosicaoTituloPorAgente>();
//		LocalDate dataRefAux = cfDTO.dataCotacao();
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