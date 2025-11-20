package com.arief.warehouse.warehouse_api.repository;

import com.arief.warehouse.warehouse_api.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
