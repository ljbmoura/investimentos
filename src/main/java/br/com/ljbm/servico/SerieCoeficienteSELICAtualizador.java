package br.com.ljbm.servico;

import br.com.ljbm.dto.CotacaoFundoDTO;
import br.com.ljbm.dto.PeriodoRemuneracaoSELICDTO;
import br.com.ljbm.modelo.Aplicacao;
import br.com.ljbm.modelo.SerieCoeficienteSELIC;
import br.com.ljbm.repositorio.AplicacaoRepo;
import br.com.ljbm.repositorio.FundoInvestimentoRepo;
import br.com.ljbm.repositorio.SerieCoeficienteSELICRepo;
import br.com.ljbm.servico.selic.Selic;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Grouped;
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

	@Value (value = "${aplicacao.topicos.periodo-remuneracao-selic}")
	private String TOPICO_PERIODO_REMUNERACAO_SELIC;

	@Autowired
	private AplicacaoRepo aplicacaoRepositorio;

	@Autowired
	private FundoInvestimentoRepo fundoInvestimentoRepositorio;

	@Autowired
	private SerieCoeficienteSELICRepo serieCoeficienteSELICRepositorio;

	@Autowired
	private Selic selicWS;

	@Autowired
	public void carregaSerieCoeficientesSELICPipeline(@Qualifier("atualizador-serie-coeficientes-SELIC") StreamsBuilder streamsBuilder) {

		KStream<String, CotacaoFundoDTO> cotacoesPorFundo = streamsBuilder.stream
			(TOPICO_COTACOES_POR_FUNDO); // , Consumed.with(Serdes.String(), new JSONSerde<CotacaoFundoDTO>()));

		cotacoesPorFundo.peek((ideFundo, cotacao)-> log.debug("Cotação por Fundo k={} v={} recebida", ideFundo, cotacao));

 		cotacoesPorFundo
				.map( (fundoIde, cotacao) -> new KeyValue<>(cotacao.dataCotacao(), fundoIde))

				.flatMap( (dataCotacao, fundoIde) -> {
					var fi =  fundoInvestimentoRepositorio.findById(Long.valueOf(fundoIde));
					if (fi.isPresent()) {
						Aplicacao filtro = new Aplicacao();
						filtro.setFundoInvestimento (fi.get());
						return aplicacaoRepositorio.findAll(Example.of(filtro))
								.stream()
								.filter(a -> a.getSaldoCotas().compareTo(BigDecimal.ZERO) > 0)
								.map(a -> new KeyValue<>(a.getDataCompra().toString(), dataCotacao.toString()) )
								.toList();
					} else {
						return Collections.emptyList();
					}
				})

				.groupByKey(Grouped.with(Serdes.String(), Serdes.String()))

				.reduce((d, v) -> v)

				.toStream()

				.map( (inicio, fim) -> new KeyValue<>(inicio, new PeriodoRemuneracaoSELICDTO(LocalDate.parse(inicio), LocalDate.parse(fim))))

				.peek((k, v) -> log.debug("Enviando k={} v={} para {}", k, v, TOPICO_PERIODO_REMUNERACAO_SELIC))

				.to(TOPICO_PERIODO_REMUNERACAO_SELIC)
		;
	}

	@KafkaListener(
			id = "pccrSELIC",
			topics = "periodo-remuneracao-selic",
			groupId = "pccrSELICGroup",
			concurrency = "3") // pois o tópico foi criado com 3 partições
	@Transactional
	public void obtemCoeficienteRemuneracaoSELIC (ConsumerRecord<String, PeriodoRemuneracaoSELICDTO> mensagem) {
//	executorService.submit( () -> {
		var periodo = mensagem.value();
		log.debug("k={} v={} recebida da partição {}", mensagem.key(), periodo, mensagem.partition());
        if (!serieCoeficienteSELICRepositorio.existeCoeficienteSELIC(periodo.inicio(), periodo.fim())) {
			try {
				BigDecimal fatorRemuneracaoAcumuladaSELIC = selicWS.fatorAcumuladoSelic(periodo.inicio(), periodo.fim());
				SerieCoeficienteSELIC remuneracao = new SerieCoeficienteSELIC(periodo.inicio(), periodo.fim(), fatorRemuneracaoAcumuladaSELIC);
				log.debug("salvando {}", remuneracao);
				serieCoeficienteSELICRepositorio.save(remuneracao);
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
			}
        }
//	});
	}

}