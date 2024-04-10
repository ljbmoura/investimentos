package br.com.ljbm.servico.selic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.json.Json;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client RestFul para recuperação do fator acumulado da Taxa Selic.
 * A Interface web correspondente está disponível em: http://www.bcb.gov.br/htms/selic/selicacumul.asp
 * 
 * A URL completa do serviço é: 
 * 	https://www3.bcb.gov.br/selic/rest/fatoresAcumulados/pub/search
 * 	?parametrosOrdenacao=[{"nome":"periodo","decrescente":false}]&page=1&pageSize=20
 *
 *	exemplo body (json):{campoPeriodo: "periodo", dataInicial: "19/04/2007", dataFinal: "06/04/2018"}
 *
 */
@Slf4j
@Service
public class Selic {

//private static final String HTTP_WWW3_BCB_GOV_BR_host = "http://localhost:8080";
private static final String HTTP_WWW3_BCB_GOV_BR_host = "https://www3.bcb.gov.br";

	//	private static final String WS_BC_FATORES_ACUMULADOS = "/selic/rest/fatoresAcumulados/pub/search";
	private static final String WS_BC_FATORES_ACUMULADOS = "/novoselic/rest/fatoresAcumulados/pub/search";//?parametrosOrdenacao=[{\"nome\":\"periodo\",\"decrescente\":false}]&page=1&pageSize=20";
	                                                        
	private static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final RestClient client;

	public Selic(RestClient.Builder restClientBuilder) {
		ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
				.withReadTimeout(Duration.ofSeconds(2));
		client = restClientBuilder
				.baseUrl(HTTP_WWW3_BCB_GOV_BR_host)
				.requestFactory(ClientHttpRequestFactories.get(settings)).build();
	}

	public BigDecimal fatorAcumuladoSelic(final LocalDate inicio, final LocalDate termino) {
		String jsonParams = Json.createObjectBuilder()
				.add("campoPeriodo", "periodo")
				.add("dataInicial", inicio.format(FORMATO_DATA_BR))
				.add("dataFinal", termino.format(FORMATO_DATA_BR))
//				.add("mes", "")
//				.add("ano", "")
				.build().toString();

		String output = client.post()
			.uri(WS_BC_FATORES_ACUMULADOS)
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
			.body(jsonParams)
			.header("Connection", "Keep-Alive")
			.header("Host", "www3.bcb.gov.br")
			.header("Content-Length", "78")
			.retrieve()
//					.onStatus(status -> status.value() != HttpURLConnection.HTTP_OK, (request, response) -> {
//						throw new RuntimeException("Failed : HTTP error code : " + status.value());
//					})
			.body(String.class);

		log.debug(output);

		//{"totalItems":1,"registros":[{"periodo":"19/04/2007 a 21/06/2018","fator":3.11506402336271,"fatorFormatado":"3,11506402336271"}],"observacoes":[],"dataAtual":"06/01/2019 às 18:50:51"}						
		Pattern pattern = Pattern.compile("\"fator\":([\\d|\\.]+)\\,");
        assert output != null;
        Matcher matcher = pattern.matcher(output);
		
		if (matcher.find()) {
			return new BigDecimal(matcher.group(1));
		}
		else {
			pattern = Pattern.compile("\"fatorFormatado\":\"([\\d|\\,]+)\"}");
			matcher = pattern.matcher(output); 
			if (matcher.find()) {
				return new BigDecimal(matcher.group(1).replaceAll(",",	"."));
			}
		}
		throw new RuntimeException("Failed: Fator has not been found in the HTTP Response.");
	}
}