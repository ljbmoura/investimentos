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

}
