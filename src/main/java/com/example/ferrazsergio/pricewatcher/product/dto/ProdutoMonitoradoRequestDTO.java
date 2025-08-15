package com.example.ferrazsergio.pricewatcher.product.dto;

import com.example.ferrazsergio.pricewatcher.product.model.CanalNotificacao;

import java.math.BigDecimal;

public record ProdutoMonitoradoRequestDTO(
        String nome,
        String url,
        BigDecimal precoAlvo,
        CanalNotificacao canalNotificacao,
        String telefone
) {}