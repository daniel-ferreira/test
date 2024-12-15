package com.group.test.domain;

public interface PedidoService {

    Pedido criarPedido(String descricao);
    Pedido consultarPedido(String id);

}
