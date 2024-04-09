package br.com.ljbm.servico;

import br.com.ljbm.dto.CotacaoFundoDTO;
import br.com.ljbm.dto.TreasuryBondsInfoDTO;
import br.com.ljbm.modelo.FundoInvestimento;
import br.com.ljbm.modelo.TipoFundoInvestimento;
import br.com.ljbm.repositorio.FundoInvestimentoRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class CotacaoFundosIntegracao {
	private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String URL_PORTAL_BB = "https://www37.bb.com.br/portalbb/tabelaRentabilidade/rentabilidade/";
    
    private static final String URL_treasurybondsinfo = "https://www.tesourodireto.com.br/json/br/com/b3/tesourodireto/service/api/treasurybondsinfo.json";

	private final RestClient restClientPortalBB;

	private final FundoInvestimentoRepo fundoInvestimentoRepo;

	private final CotacaoFundoProdutor cotacaoFundoProdutor;

    public CotacaoFundosIntegracao(
			RestClient.Builder restClientBuilder,
//			SslBundles sslBundles,
			FundoInvestimentoRepo fundoInvestimentoRepo,
			CotacaoFundoProdutor cotacaoFundoProdutor) {

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
//			.withSslBundle(sslBundles.getBundle("mybundle"))
            .withReadTimeout(Duration.ofSeconds(5)
		);
        this.restClientPortalBB = restClientBuilder.baseUrl(URL_PORTAL_BB).requestFactory(ClientHttpRequestFactories.get(settings)).build();
		this.fundoInvestimentoRepo = fundoInvestimentoRepo;
		this.cotacaoFundoProdutor = cotacaoFundoProdutor;
	}

    public void obtemCotacaoFundosBB() {
		var cotacoes = new ArrayList<CotacaoFundoDTO>();
		cotacoes.addAll(getCotasFundosBB("gfi7,802,9085,9089,1.bbx?tipo=1&nivel=1000", 2));
		cotacoes.addAll(getCotasFundosBB("gfi7,802,9085,9089,1.bbx?tipo=11&nivel=1000", 1));
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

		TreasuryBondsInfoDTO root = om.readValue(jsonString, TreasuryBondsInfoDTO.class);
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
		var filtro = new FundoInvestimento();
		filtro.setTipoFundoInvestimento(tfi);
        for (FundoInvestimento f : fundoInvestimentoRepo.findAll(Example.of(filtro))) {
            cotacoes.stream()
				.filter(d -> d.nomeFundo().trim().equals(f.getNome().trim()))
                .findFirst().ifPresent(cotacaoFI -> {
					// Fixme talvez seja o caso de não usar chave neste tópico para que a
					// distribuição nas partições seja round-robin.
					// Poderia-se incluir o id do agente financeiro no DTO
                	cotacaoFundoProdutor.sendMessage(f.getIde().toString(), cotacaoFI);
        		});
        }
    }

}