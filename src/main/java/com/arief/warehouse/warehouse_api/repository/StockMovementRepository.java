package com.arief.warehouse.warehouse_api.repository;

import com.arief.warehouse.warehouse_api.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByVariantIdOrderByCreatedAtDesc(Long variantId);
}
