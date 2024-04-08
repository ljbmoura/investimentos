package br.com.ljbm.configuracao;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.streams.StreamsConfig.*;

@Configuration
@Slf4j
public class ConfiguracaoKafka {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

/*    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAdress);
        return new KafkaAdmin(configs);
    }*/

//    @Bean
//    public RecordMessageConverter converter() {
//        return new JsonMessageConverter();
//    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration ksConfiguracaoBase(KafkaProperties streamsConfig) {
        Map<String, Object> props = new HashMap<>();
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        var adminProps = streamsConfig.buildAdminProperties(null);
        props.putAll(adminProps);
        props.forEach((k, v) -> log.debug("propriedade '{}' ={}", k, v));
        return new KafkaStreamsConfiguration(props);
    }
/*
    @Bean (name = "atualizador-serie-coeficientes-SELIC")
    public StreamsBuilderFactoryBean fabricaParaAtualizadorSerieCoeficientesSELIC(KafkaStreamsConfiguration ksConfiguracaoBase) {
        Map<String, Object> props = new HashMap<>();
        ksConfiguracaoBase.asProperties().forEach((k, v) -> props.put(String.valueOf(k), v));
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "atualizador-serie-coeficientes-SELIC");
        KafkaStreamsConfiguration configAplicacao = new KafkaStreamsConfiguration(props);
        return new StreamsBuilderFactoryBean(configAplicacao);
    }
*/
}
