package com.arief.warehouse.warehouse_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "item_variants",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_item_variant_sku", columnNames = "sku")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ItemVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // setiap variant milik satu Item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false, length = 100)
    private String sku;

    private String color;

    private String size;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;
}
