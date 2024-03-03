package br.com.ljbm.servico;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.ljbm.modelo.CotacaoFundo;
import br.com.ljbm.repositorio.CotacaoFundoRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
//import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.ljbm.dto.CotacaoFundoDTO;
import br.com.ljbm.modelo.FundoInvestimento;
import br.com.ljbm.modelo.TipoFundoInvestimento;
import br.com.ljbm.repositorio.FundoInvestimentoRepo;
import br.com.ljbm.servico.treasurybondsinfo.Root;
import br.com.ljbm.repositorio.CotacaoFundoRepo;


@Service
public class CotacaoFundosIntegracao {
	private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String URL_PORTAL_BB = "https://www37.bb.com.br/portalbb/tabelaRentabilidade/rentabilidade/";
    
    private static final String URL_treasurybondsinfo = "https://www.tesourodireto.com.br/json/br/com/b3/tesourodireto/service/api/treasurybondsinfo.json";

	private final Logger logger = LoggerFactory.getLogger(CotacaoFundosIntegracao.class);

	private final RestClient restClientPortalBB;

	private final FundoInvestimentoRepo fundoInvestimentoRepo;

	private final CotacaoFundoRepo cotacaoFundoRepo;


	@Autowired
	private KafkaTemplate<Object, Object> cotacaoFundoProdutor;

    public CotacaoFundosIntegracao(
			RestClient.Builder restClientBuilder,
//			SslBundles sslBundles,
			CotacaoFundoRepo cotacaoFundoRepo,
			FundoInvestimentoRepo fundoInvestimentoRepo) {

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
//			.withSslBundle(sslBundles.getBundle("mybundle"))
            .withReadTimeout(Duration.ofSeconds(5)
		);
        this.restClientPortalBB = restClientBuilder.baseUrl(URL_PORTAL_BB).requestFactory(ClientHttpRequestFactories.get(settings)).build();
		this.cotacaoFundoRepo = cotacaoFundoRepo;
		this.fundoInvestimentoRepo = fundoInvestimentoRepo;
	}

    public void obtemCotacaoFundosBB() {
		var cotacoes = new ArrayList<CotacaoFundoDTO>();
		cotacoes.addAll(getCotasFundosBB("gfi7,802,9085,9089,1.bbx?tipo=1&nivel=1000", 2));
//		cotacoes.addAll(getCotasFundosBB("gfi7,802,9085,9089,1.bbx?tipo=11&nivel=1000", 1));
		filtraCotacoesEPublicaTopico(cotacoes, TipoFundoInvestimento.Acoes);
    }

    public void obtemCotacaoFundosTesouroDireto() throws IOException {
		var cotacoes = getCotasFundosTesouroDireto();
		filtraCotacoesEPublicaTopico(cotacoes, TipoFundoInvestimento.TesouroDireto);
    }

	private List<CotacaoFundoDTO> getCotasFundosBB(String uriTabelasCotasFundosBB, int indiceTabela) {
		Document doc = Jsoup.parse(Objects.requireNonNull(
			restClientPortalBB.get()
				.uri(uriTabelasCotasFundosBB)
				.accept(MediaType.TEXT_HTML)
				.retrieve().body(String.class))
		);
		return doc.getElementsByTag("tbody").get(indiceTabela).children().stream()
			.map((e) -> {
				var c = e.children();
				String html = c.get(0).html();
				return new CotacaoFundoDTO(
					html.substring(0, html.indexOf("<sup")),
					LocalDate.parse(c.get(10).html(), DATA_BR),
					new BigDecimal(c.get(11).html().replace(",", ".")));
			})
//    		.peek(System.out::println)
			.toList();
	}

	private List<CotacaoFundoDTO> getCotasFundosTesouroDireto() throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		String jsonString = restClientPortalBB.get()
				.uri(URL_treasurybondsinfo)
				.accept(MediaType.TEXT_HTML)
				.retrieve().body(String.class);

		Root root = om.readValue(jsonString, Root.class);
		LocalDateTime dataCotacao = Instant.ofEpochMilli(root.response.trsrBondMkt.qtnDtTm.getTime())
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
		return root.response.trsrBdTradgList.stream()
				.map (td ->
					new CotacaoFundoDTO(td.trsrBd.nm, dataCotacao.toLocalDate(),
						BigDecimal.valueOf(td.trsrBd.untrRedVal)))
				.toList();
	}

	private void filtraCotacoesEPublicaTopico (List<CotacaoFundoDTO> cotacoes, TipoFundoInvestimento tfi) {
		var modelo = new FundoInvestimento();
		modelo.setTipoFundoInvestimento(tfi);
		Example<FundoInvestimento> _modelo = Example.of(modelo);
        for (FundoInvestimento f : fundoInvestimentoRepo.findAll(_modelo).subList(0, 1)) {
            cotacoes.stream()
				.filter(d -> d.nomeFundo().trim().equals(f.getNome().trim()))
                .findFirst().ifPresent(cd -> {
                	cotacaoFundoProdutor.send("cotacoes-fundos", cd);
					logger.info("{} enviada.", cd);
        		});
        }
    }

	@KafkaListener(id = "cotacaoFundosGroup", topics = "cotacoes-fundos")
	public void listen(CotacaoFundoDTO cf) {
		logger.info("{} recebida", cf);
		var modelo = new FundoInvestimento();
		modelo.setNome(cf.nomeFundo());
		Example<FundoInvestimento> _modelo = Example.of(modelo);
        for (FundoInvestimento fi : fundoInvestimentoRepo.findAll(_modelo)) {
            logger.info("atualizando cotação do {}", fi);
            var c = new CotacaoFundo(cf.dataCotacao(), cf.valorCota(), fi);
            try {
				CotacaoFundo cMerged = cotacaoFundoRepo.mergePorDataFundo(c);
//                logger.info("{} sincronizada.", cMerged);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }

}