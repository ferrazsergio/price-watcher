package com.example.ferrazsergio.pricewatcher.product.dto;

import com.example.ferrazsergio.pricewatcher.product.model.CanalNotificacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoMonitoradoResponseDTO(
        Long id,
        String nome,
        String url,
        BigDecimal precoAlvo,
        BigDecimal precoAtual,
        LocalDateTime dataUltimaVerificacao,
        CanalNotificacao canalNotificacao,
        String telefone
) {}