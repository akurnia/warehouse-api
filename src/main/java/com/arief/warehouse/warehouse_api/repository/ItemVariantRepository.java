package com.arief.warehouse.warehouse_api.repository;

import com.arief.warehouse.warehouse_api.entity.ItemVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {

    List<ItemVariant> findByItemId(Long itemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from ItemVariant v where v.id = :id")
    Optional<ItemVariant> findByIdForUpdate(@Param("id") Long id);
}
