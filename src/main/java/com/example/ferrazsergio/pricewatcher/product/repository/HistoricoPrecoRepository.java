package com.example.ferrazsergio.pricewatcher.product.repository;

import com.example.ferrazsergio.pricewatcher.product.model.HistoricoPreco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface HistoricoPrecoRepository extends JpaRepository<HistoricoPreco, Long> {
    List<HistoricoPreco> findByProdutoIdOrderByDataConsultaDesc(Long produtoId);
}
