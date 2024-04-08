package br.com.ljbm.servico;

import br.com.ljbm.modelo.Aplicacao;
import br.com.ljbm.repositorio.AplicacaoRepo;
import br.com.ljbm.repositorio.FundoInvestimentoRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;


@Slf4j
@Service
//@RequiredArgsConstructor
public class SerieCoeficienteSELICAtualizador {

	@Value (value = "${aplicacao.topicos.cotacoes-fundos}")
	private String TOPICO_COTACOES_POR_FUNDO;

	@Autowired
	private AplicacaoRepo aplicacaoRepositorio;

	@Autowired
	private FundoInvestimentoRepo fundoInvestimentoRepositorio;

	@Autowired
	public void carregaSerieCoeficientesSELICPipeline(@Qualifier("atualizador-serie-coeficientes-SELIC") StreamsBuilder streamsBuilder) {

		KStream<String, String> cotacoesPorFundo = streamsBuilder.stream
				(TOPICO_COTACOES_POR_FUNDO, Consumed.with(Serdes.String(), Serdes.String()) );

		cotacoesPorFundo.peek((ideFundo, cotacao)-> log.debug("chave fundo={} cotação={} recebida da partição", ideFundo, cotacao));

//		chave 27 valor 2006-05-30
//		chave 27 valor 2006-09-25
//		chave fundo=27 cotação={"nomeFundo":"Ações Petrobras","dataCotacao":[2024,3,28],"valorCota":23.996366000} recebida da partição

 		cotacoesPorFundo

				.map( (fundoIde, cotacao) -> {
					// FIXME refazer quando conseguir uma KStream<String, CotacaoFundoDTO>
					int posIniData = cotacao.indexOf("[");
					int posFimData = cotacao.indexOf("]", posIniData + 1);
					String[] _dataCotacao = cotacao.substring(posIniData + 1, posFimData).split(",");
					var dataCotacao = LocalDate.of(Integer.parseInt(_dataCotacao[0]), Integer.parseInt(_dataCotacao[1]), Integer.parseInt(_dataCotacao[2]));
					return new KeyValue<>(dataCotacao.toString(), fundoIde) ;
				})

				.flatMap( (dataCotacao, fundoIde) -> {
					var fi =  fundoInvestimentoRepositorio.findById(Long.valueOf(fundoIde));
					if (fi.isPresent()) {
						Aplicacao filtro = new Aplicacao();
						filtro.setFundoInvestimento (fi.get());
						return aplicacaoRepositorio.findAll(Example.of(filtro))
								.stream()
								.filter(a -> a.getSaldoCotas().compareTo(BigDecimal.ZERO) > 0)
								.map(a -> new KeyValue<>(a.getDataCompra().toString(), dataCotacao) )
								.toList();
					} else {
						return Collections.emptyList();
					}
				})

				.groupBy((dataCompra, dataCotacao) -> dataCompra)
				.reduce((d, v) -> v)
				.toStream()

				.peek((k, v) -> log.info("chave {} valor {}", k, v))

				.to("periodo-calculo-coeficiente-remuneracao-selic");
	}

	@KafkaListener(
			id = "pccrSELIC",
			topics = "periodo-calculo-coeficiente-remuneracao-selic",
			groupId = "pccrSELICGroup",
			concurrency = "3") // pois o tópico foi criado com 3 partições
	@Transactional
	public void obtemCoeficienteRemuneracaoSELIC (ConsumerRecord<String, Object> mensagem) {
//		executorService.submit( () -> {
		var dataCompra = mensagem.key();
		var dataCotacao = mensagem.value();
		var particao = mensagem.partition();
		log.info("k={} v={} recebida da partição {}", dataCompra, dataCotacao, particao);
//			try {
//				var cf = new CotacaoFundo(cfDTO.dataCotacao(), cfDTO.valorCota(),
//						fundoInvestimentoRepo.getReferenceById(Long.valueOf(chaveFundoInvestimento)));
//				CotacaoFundo cMerged = cotacaoFundoRepo.mergePorDataFundo(cf);
//				log.info("sincronizada {}.", cMerged);
//			} catch (DataIntegrityViolationException e) {
//				log.error(e.getLocalizedMessage());
//			}
//		});
	}

}