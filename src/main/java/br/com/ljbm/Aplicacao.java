package br.com.ljbm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;

@SpringBootApplication
public class
Aplicacao {
//	private final Logger logger = LoggerFactory.getLogger(Aplicacao.class);

	public static void main(String[] args) {

		SpringApplication.run(Aplicacao.class, args);
	}

	@Bean
	public RecordMessageConverter converter() {
		return new JsonMessageConverter();
	}

//	@KafkaListener(id = "cotacaoFundosGroup", topics = "cotacoes-fundos")
//	public void listen(CotacaoFundoDTO cf) {
//		logger.info("{} recebida", cf);
//		FundoInvestimento fi = new FundoInvestimento();
//		fi.setIde(fi.getIde());
//		var c = new CotacaoFundo(
//				cf.dataCotacao(),
//				cf.valorCota(),
//				fi);
//		try {
//			c = cotacaoFundoRepo.mergePorDataFundo(c);
//			logger.info("{} sincronizada.", c);
//		} catch (DataIntegrityViolationException e) {
//			logger.error(e.getLocalizedMessage());
//		}
//	}
}
