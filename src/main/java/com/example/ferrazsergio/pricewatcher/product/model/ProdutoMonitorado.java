package com.example.ferrazsergio.pricewatcher.product.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProdutoMonitorado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String url;
    private BigDecimal precoAlvo;
    private BigDecimal precoAtual;
    private LocalDateTime dataUltimaVerificacao;

    @Enumerated(EnumType.STRING)
    private CanalNotificacao canalNotificacao;

    private String telefone;
}
