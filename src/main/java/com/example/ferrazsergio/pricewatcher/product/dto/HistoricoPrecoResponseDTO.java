package com.example.ferrazsergio.pricewatcher.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricoPrecoResponseDTO(
        BigDecimal preco,
        LocalDateTime dataConsulta
) {}