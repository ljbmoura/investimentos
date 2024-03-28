package br.com.ljbm.servico;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

@Service
@Slf4j
public class CotacaoFundoProdutor {
    private static final String TOPICO = "cotacoes-fundos";

    @Autowired private
    KafkaTemplate<String, Object> kafkaTemplate;
    //@Autowired private
    //KafkaProducer<String, Object> produtor;
    public void sendMessage(String chave, Object valor) {
/*        produtor.send( new ProducerRecord<>(TOPICO, chave, valor),
                (event, ex) -> {
                    if (ex != null)
                        ex.printStackTrace();
                    else
                        logger.info("Produced event to topic %s: key = %-10s value = %s%n", TOPICO, chave, valor);
                });*/
        kafkaTemplate.send(TOPICO, chave, valor);
        log.info("k={} v={} enviada", chave, valor);
    }
}
