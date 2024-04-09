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
public class CotacaoFundoAtualizador {

	private final FundoInvestimentoRepo fundoInvestimentoRepo;

	private final CotacaoFundoRepo cotacaoFundoRepo;

	ExecutorService executorService = Executors.newFixedThreadPool(30);

	@KafkaListener (
			id = "cotacaoFundo",
			topics = "cotacoes-fundos",
			groupId = "cotacaoFundoGroup",
			concurrency = "3") // pois o tópico foi criado com 3 partições
	@Transactional
	public void atualizacaoCotacaoFundoListen (ConsumerRecord<String, CotacaoFundoDTO> mensagem) {
		executorService.submit( () -> {
			var cfDTO = mensagem.value();
			var chaveFundoInvestimento = mensagem.key();
			var particao = mensagem.topic();
			log.debug("k={} v={} recebida da partição {}", chaveFundoInvestimento, cfDTO, particao);
			try {
				var cf = new CotacaoFundo(cfDTO.getDataCotacao(), cfDTO.getValorCota(),
						fundoInvestimentoRepo.getReferenceById(Long.valueOf(chaveFundoInvestimento)));
				CotacaoFundo cMerged = cotacaoFundoRepo.mergePorDataFundo(cf);
				log.info("sincronizada {}.", cMerged);

			} catch (DataIntegrityViolationException e) {
				log.error(e.getLocalizedMessage());
			}
		});
	}

}