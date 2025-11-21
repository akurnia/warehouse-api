INSERT INTO items (name, description, active, created_at, updated_at)
VALUES ('T-Shirt', 'Basic cotton T-Shirt', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO item_variants (item_id, sku, color, size, price, stock_quantity, created_at, updated_at)
VALUES (1, 'TSHIRT-BLACK-M', 'Black', 'M', 99000.0, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO stock_movements (variant_id, type, quantity_change, reason, created_at, updated_at)
VALUES (1, 'OUT', -3, 'SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
