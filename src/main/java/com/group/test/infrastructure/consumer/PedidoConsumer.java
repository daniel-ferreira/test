package com.group.test.infrastructure.consumer;

import com.group.test.domain.Pedido;
import com.group.test.domain.StatusPedido;
import com.group.test.infrastructure.repository.PedidoRepository;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "pedido-topic", groupId = "pedido-group")
public class PedidoConsumer {

    private final PedidoRepository pedidoRepository;

    public PedidoConsumer(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @KafkaHandler
    public void consumePedido(Pedido pedido) {
        pedido.setStatus(StatusPedido.ENVIADO_TRANSPORTADORA);
        pedidoRepository.save(pedido);
    }

}
