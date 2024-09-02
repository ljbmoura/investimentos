package br.com.ljbm.servico;

import br.com.ljbm.dto.CotacaoFundoDTO;
import br.com.ljbm.dto.TreasuryBondsInfoDTO;
import br.com.ljbm.modelo.FundoInvestimento;
import br.com.ljbm.modelo.TipoFundoInvestimento;
import br.com.ljbm.repositorio.FundoInvestimentoRepo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Base64;

@Slf4j
@Service
public class CotacaoFundosIntegracao {
	private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String URL_PORTAL_BB = "https://www37.bb.com.br/portalbb/tabelaRentabilidade/rentabilidade/";
    
    private static final String URL_treasurybondsinfo = "https://www.tesourodireto.com.br/json/br/com/b3/tesourodireto/service/api/treasurybondsinfo.json";

//	private final RestClient restClientPortalBB;
	
	private final RestClient.Builder restClientBuilder;

	private final FundoInvestimentoRepo fundoInvestimentoRepo;

	private final CotacaoFundoProdutor cotacaoFundoProdutor;

//	private SslBundles sslBundles;

    public CotacaoFundosIntegracao(
			RestClient.Builder restClientBuilder,
			SslBundles sslBundles,
			FundoInvestimentoRepo fundoInvestimentoRepo,
			CotacaoFundoProdutor cotacaoFundoProdutor) {

    	this.restClientBuilder = restClientBuilder; 
//    	this.sslBundles = sslBundles; 
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
//		cotacoes.stream().forEach(c -> System.out.println(c));
		filtraCotacoesEPublicaTopico(cotacoes, TipoFundoInvestimento.TesouroDireto);
    }

	private List<CotacaoFundoDTO> getCotasFundosBB(String uriTabelasCotasFundosBB, int indiceTabela) {
		
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
//			.withSslBundle(sslBundles.getBundle("mybundle"))
            .withReadTimeout(Duration.ofSeconds(5)
		);
        var restClientPortalBB = restClientBuilder
        		.baseUrl(URL_PORTAL_BB)
        		.requestFactory(ClientHttpRequestFactories.get(settings)).build();
		
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

	private List<CotacaoFundoDTO> getCotasFundosTesouroDireto() throws IOException {
		ObjectMapper om = new ObjectMapper();
		
		 ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
//					.withSslBundle(sslBundles.getBundle("mybundle"))
		            .withReadTimeout(Duration.ofSeconds(5)
				);
		var restClient = restClientBuilder
		        		.baseUrl(URL_treasurybondsinfo)
		        		.requestFactory(ClientHttpRequestFactories.get(settings)).build();
		String jsonString = 
			restClient.get()
//				.uri(URL_treasurybondsinfo)
				.accept(MediaType.ALL)
				.retrieve().body(String.class);
		
//		String jsonString = 
//			Files.readString(
//				Paths.get("C:\\desenv\\github\\investimentos\\src\\main\\resources\\static\\data.json"), StandardCharsets.UTF_8);
		

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
	
	
	private void getPrecosTesouroDiretoAMBIMA () {
		
//		GET https://api-sandbox.anbima.com.br/feed/precos-indices/v1/titulos-publicos/mercado-secundario-TPF
//			https://api        .anbima.com.br/feed/precos-indices/v1/titulos-publicos/mercado-secundario-TPF
		
		
//		Nome						Descrição									Client ID    / Client Secret	Status		Access Tokens
//		Luciano Jose Bravo de Moura	uso pessoal apenas para leitura de preços	aqFOmn9oeJ2T / l9fpgpHQsJgu		Aprovada	detalhes		
//		Basic base64(aqFOmn9oeJ2T:l9fpgpHQsJgu)
		
//		https://api.sandbox.anbima.com.br
//		https://api.anbima.com.br
		
//		https://api.anbima.com.br/oauth/access-token
//		{
//			  "method": "POST",
//			  "transformRequest": [
//			    null
//			  ],
//			  "transformResponse": [
//			    null
//			  ],
//			  "jsonpCallbackParam": "callback",
//			  "url": "https://api.anbima.com.br/oauth/access-token",
//			  "headers": {
//			    "Accept": "application/json",
//			    "Authorization": "Basic YXFGT21uOW9lSjJUOmw5ZnBncEhRc0pndQ==",
//			    "Content-Type": "application/json;charset=utf-8"
//			  },
//			  "data": "{\n\"grant_type\": \"client_credentials\"\n}",
//			  "timeout": {}
//			}
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



    public static void main(String[] args) {
        // Texto original
        String textoOriginal = "aqFOmn9oeJ2T:l9fpgpHQsJgu";
        System.out.println("Texto original: " + textoOriginal);

        // Codificando para Base64
        String textoCodificado = Base64.getEncoder().encodeToString(textoOriginal.getBytes());
        System.out.println("Texto em Base64: " + textoCodificado);

        // Decodificando de Base64
        byte[] decodedBytes = Base64.getDecoder().decode(textoCodificado);
        String textoDecodificado = new String(decodedBytes);
        System.out.println("Texto decodificado: " + textoDecodificado);
    }

	
	
}
	

