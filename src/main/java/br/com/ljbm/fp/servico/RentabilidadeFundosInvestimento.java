package br.com.ljbm.fp.servico;

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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.ljbm.fp.dto.CotacaoFundoDTO;
import br.com.ljbm.fp.modelo.CotacaoFundo;
import br.com.ljbm.fp.modelo.FundoInvestimento;
import br.com.ljbm.fp.modelo.TipoFundoInvestimento;
import br.com.ljbm.fp.repositorio.CotacaoFundoRepo;
import br.com.ljbm.fp.repositorio.FundoInvestimentoRepo;
import br.com.ljbm.fp.servico.treasurybondsinfo.Root;

@Service
public class RentabilidadeFundosInvestimento {
	private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String URL_PORTAL_BB = "https://www37.bb.com.br/portalbb/tabelaRentabilidade/rentabilidade/";
    
    private static final String URL_treasurybondsinfo = "https://www.tesourodireto.com.br/json/br/com/b3/tesourodireto/service/api/treasurybondsinfo.json";

	private final RestClient restClientPortalBB;

	private final CotacaoFundoRepo cotacaoFundoRepo;

	private final FundoInvestimentoRepo fundoInvestimentoRepo;

    public RentabilidadeFundosInvestimento(
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
		cotacoes.addAll(getCotasFundosBB("gfi7,802,9085,9089,1.bbx?tipo=11&nivel=1000", 1));
		fundoInvestimentoRepo.findAll().forEach(f -> {
			//System.out.println(f.getNome());
			cotacoes.stream().filter(d -> d.nomeFundo().trim().equals(f.getNome().trim()))
					.findFirst().ifPresent( cd -> {
				FundoInvestimento fi = new FundoInvestimento();
				fi.setIde(f.getIde());
				var c = new CotacaoFundo(
						cd.dataCotacao(),
						cd.valorCota(),
						fi);
				try {
					c = cotacaoFundoRepo.mergePorDataFundo(c);
					System.out.printf("%s persistindo.\n", c);
				} catch (DataIntegrityViolationException e) {
//					System.out.println(e.getLocalizedMessage());
				}
			});
		});
    }

	private List<CotacaoFundoDTO> getCotasFundosBB(String uriTabelasCotasFundosBB, int indiceTabela) {
		Document doc = Jsoup.parse(Objects.requireNonNull(
			restClientPortalBB.get()
				.uri(uriTabelasCotasFundosBB)
				.accept(MediaType.TEXT_HTML)
				.retrieve().body(String.class))
		);

//    	Elements footer = doc.getElementsByClass("tb_accordion");
		Elements footer = doc.getElementsByTag("tbody");

		return footer.get(indiceTabela).children().stream()
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
	
    public void obtemCotacaoTesouroDireto() throws IOException {
   	
		ObjectMapper om = new ObjectMapper();
		String jsonString = 
//			Files.readString(
//				Paths.get("C:\\Users\\luciana\\OneDrive\\luc&luca\\luciano\\git\\investimentos.bb\\src\\main\\resources\\static\\data.json"), StandardCharsets.UTF_8);
	    	restClientPortalBB.get()
				.uri(URL_treasurybondsinfo)
				.accept(MediaType.TEXT_HTML)
				.retrieve().body(String.class);
		Root root = om.readValue(jsonString, Root.class);
		var modelo = new FundoInvestimento();
		modelo.setTipoFundoInvestimento(TipoFundoInvestimento.TesouroDireto);
		Example<FundoInvestimento> _modelo = Example.of(modelo);
		fundoInvestimentoRepo.findAll(_modelo).forEach(f -> {
			System.out.println(f.getNome());
			LocalDateTime dataCotacao = Instant.ofEpochMilli(root.response.trsrBondMkt.qtnDtTm.getTime())
				      .atZone(ZoneId.systemDefault())
				      .toLocalDateTime(); 
			root.response.trsrBdTradgList.stream().filter(d -> d.trsrBd.nm.trim().equals(f.getNome().trim()))
					.findFirst().ifPresent( cd -> {
				var c = new CotacaoFundo(
						dataCotacao.toLocalDate(),
						BigDecimal.valueOf(cd.trsrBd.untrRedVal),
						f);
				try {
					c = cotacaoFundoRepo.mergePorDataFundo(c);
//					System.out.printf("%s persistindo.\n", c);
				} catch (DataIntegrityViolationException e) {
//					System.out.println(e.getLocalizedMessage());
				}
			});
		});
    }
    

	
}