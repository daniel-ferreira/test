package com.group.test.infrastructure.producer;

import com.group.test.domain.Pedido;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PedidoProducer {

    private final KafkaTemplate<String, Pedido> kafkaTemplate;

    public PedidoProducer(KafkaTemplate<String, Pedido> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPedido(Pedido pedido) {
        kafkaTemplate.send("pedido-topic", pedido);
    }

}
