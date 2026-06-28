package com.rodrigolopes.inventory_service.exceptions;


public class ItemNotFoundException extends RuntimeException {

    private final String orderId;
    private final String itemId;

    public ItemNotFoundException(String orderId, String itemId) {
        super("Item %s not found".formatted(itemId));
        this.orderId = orderId;
        this.itemId = itemId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getItemId() {
        return itemId;
    }
}