package br.com.ljbm.servico;

import br.com.ljbm.modelo.Aplicacao;
import br.com.ljbm.repositorio.AplicacaoRepo;
import br.com.ljbm.repositorio.FundoInvestimentoRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;


@Slf4j
@Service
@RequiredArgsConstructor
public class SerieCoeficienteSELICAtualizador {

	@Value (value = "${aplicacao.topicos.cotacoes-fundos}")
	private String TOPICO_ORIGEM;

	private final AplicacaoRepo aplicacaoRepositorio;
	private final FundoInvestimentoRepo fiRepositorio;

/*
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
*/

	@Autowired
//	@Transactional
	void carregaSerieCoeficientesSELICPipeline(StreamsBuilder streamsBuilder) {

		KStream<String, String> cotacoesPorFundo = streamsBuilder
			.stream(TOPICO_ORIGEM, Consumed.with(Serdes.String(), Serdes.String() ));

//		chave fundo=27 cotação={"nomeFundo":"Ações Petrobras ","dataCotacao":[2024,3,28],"valorCota":23.996366000} recebida da partição
//		chave 27 valor 2006-05-30
//		chave 27 valor 2006-09-25

		cotacoesPorFundo
			//.peek((ideFundo, cotacao)-> log.info("chave fundo={} cotação={} recebida da partição", ideFundo, cotacao))
				.map( (fundoIde, cotacao) ->
						new KeyValue<>(LocalDate.of(2024, 3, 28), fundoIde) )

				.flatMap( (dataCotacao, fundoIde) -> {
					var fi =  fiRepositorio.findById(Long.valueOf(fundoIde)).get();
					Aplicacao filtro = new Aplicacao();
					filtro.setFundoInvestimento (fi);
					return aplicacaoRepositorio.findAll(Example.of(filtro))
							.stream()
							.filter(a -> a.getSaldoCotas().compareTo(BigDecimal.ZERO) > 0)
							.map(a -> new KeyValue<>(a.getDataCompra().toString(), dataCotacao.toString()) )
							.toList();
                })

				.groupBy((dataCompra, dataCotacao) -> dataCompra)
				.reduce((d, v) -> v)
				.toStream()

			.peek((k, v) -> log.info("chave {} valor {}", k, v))
		;
	}

}