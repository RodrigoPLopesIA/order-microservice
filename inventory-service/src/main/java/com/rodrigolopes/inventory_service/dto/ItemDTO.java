package com.rodrigolopes.inventory_service.dto;

import lombok.Builder;

@Builder
public record ItemDTO(String itemId, int quantity) {
}
