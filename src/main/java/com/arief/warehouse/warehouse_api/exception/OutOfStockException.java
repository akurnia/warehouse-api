package com.arief.warehouse.warehouse_api.exception;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(Long variantId, int requested, int available) {
        super(String.format(
                "Not enough stock for variant %d. Requested: %d, available: %d",
                variantId, requested, available
        ));
    }
}
