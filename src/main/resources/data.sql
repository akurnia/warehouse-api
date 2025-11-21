-- Sample Items
INSERT INTO items (id, name, description, active, created_at, updated_at)
VALUES
  (1, 'T-Shirt', 'Basic cotton T-Shirt', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'Hoodie', 'Fleece hoodie', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO item_variants (id, item_id, sku, color, size, price, stock_quantity, created_at, updated_at)
VALUES
  (1, 1, 'TSHIRT-BLACK-M', 'Black', 'M', 99000.00, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 1, 'TSHIRT-BLACK-L', 'Black', 'L', 99000.00, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 2, 'HOODIE-GREY-M', 'Grey', 'M', 199000.00, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO stock_movements (id, variant_id, type, quantity_change, reason, created_at, updated_at)
VALUES
  (1, 3, 'IN', 5, 'INITIAL_STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
