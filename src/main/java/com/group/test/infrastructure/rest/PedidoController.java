package com.group.test.infrastructure.rest;

import com.group.test.domain.Pedido;
import com.group.test.domain.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<Pedido> criarPedido(@RequestBody Map<String, String> request) {
        String descricao = request.get("descricao");
        Pedido pedido = pedidoService.criarPedido(descricao);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> consultarPedido(@PathVariable String id) {
        Pedido pedido = pedidoService.consultarPedido(id);
        return ResponseEntity.ok(pedido);
    }
}
