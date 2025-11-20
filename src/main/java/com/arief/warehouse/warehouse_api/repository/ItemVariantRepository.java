package com.arief.warehouse.warehouse_api.repository;

import com.arief.warehouse.warehouse_api.entity.ItemVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {

    List<ItemVariant> findByItemId(Long itemId);
}
