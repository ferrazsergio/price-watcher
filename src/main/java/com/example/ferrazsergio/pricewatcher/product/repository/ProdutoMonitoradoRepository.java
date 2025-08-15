package com.example.ferrazsergio.pricewatcher.product.repository;

import com.example.ferrazsergio.pricewatcher.product.model.ProdutoMonitorado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoMonitoradoRepository extends JpaRepository<ProdutoMonitorado, Long> {
}
