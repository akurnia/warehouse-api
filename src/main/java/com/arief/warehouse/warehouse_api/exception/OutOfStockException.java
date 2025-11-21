package com.arief.warehouse.warehouse_api.exception;

import lombok.Getter;

@Getter
public class OutOfStockException extends RuntimeException {

    private final Long variantId;
    private final int requested;
    private final int available;

    public OutOfStockException(Long variantId, int requested, int available) {
        super("Not enough stock for variant " + variantId +
                ". Requested: " + requested + ", available: " + available);
        this.variantId = variantId;
        this.requested = requested;
        this.available = available;
    }
}
