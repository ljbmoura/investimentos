package br.com.ljbm.servico;

import br.com.ljbm.dto.CotacaoFundoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CotacaoFundoProdutor {
    private static final String TOPICO = "cotacoes-fundos";

    private final KafkaTemplate<String, CotacaoFundoDTO> kafkaTemplate;
    public void sendMessage(String chave, CotacaoFundoDTO valor) {

        kafkaTemplate.send(new ProducerRecord<>(TOPICO, chave, valor));
        log.info("k={} v={} enviada", chave, valor);
    }
}
