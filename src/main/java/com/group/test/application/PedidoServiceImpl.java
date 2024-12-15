package com.group.test.application;

import com.group.test.domain.StatusPedido;
import com.group.test.infrastructure.producer.PedidoProducer;
import com.group.test.infrastructure.repository.PedidoRepository;
import com.group.test.domain.Pedido;
import com.group.test.domain.PedidoService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoProducer pedidoProducer;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, PedidoProducer pedidoProducer) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoProducer = pedidoProducer;
    }

    @Override
    public Pedido criarPedido(String descricao) {
        long newId = Math.abs(UUID.randomUUID().getMostSignificantBits());
        Pedido pedido = new Pedido(newId, descricao, StatusPedido.AGUARDANDO_ENVIO);
        pedidoRepository.save(pedido);
        pedidoProducer.sendPedido(pedido);
        return pedido;
    }

    @Override
    public Pedido consultarPedido(String id) {
        return pedidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }
}
