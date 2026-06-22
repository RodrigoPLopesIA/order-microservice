package com.rodrigolopes.inventory_service.dto;

import lombok.Builder;

@Builder
public record ItemDTO(String itemId, String name, int quantity, double price) {
}
