package com.rodrigolopes.inventory_service.exceptions;

import lombok.Data;

@Data
public class OutOfStockException extends RuntimeException {

    private final String orderId;
    private final String itemId;
    private final int requested;
    private final int available;

    public OutOfStockException(
            String orderId,
            String itemId,
            int requested,
            int available
    ) {
        super("Insufficient stock for item %s".formatted(itemId));
        this.orderId = orderId;
        this.itemId = itemId;
        this.requested = requested;
        this.available = available;
    }

    // getters...
}
