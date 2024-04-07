package br.com.ljbm.servico;

import br.com.ljbm.dto.CotacaoFundoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CotacaoFundoProdutor {
    @Value (value = "${aplicacao.topicos.cotacoes-fundos}")
    private String TOPICO_DESTINO;

    private final KafkaTemplate<String, CotacaoFundoDTO> kafkaTemplate;
    public void sendMessage(String chave, CotacaoFundoDTO valor) {

        kafkaTemplate.send(new ProducerRecord<>(TOPICO_DESTINO, chave, valor));
        //log.info("k={} v={} enviada", chave, valor);
    }
}
