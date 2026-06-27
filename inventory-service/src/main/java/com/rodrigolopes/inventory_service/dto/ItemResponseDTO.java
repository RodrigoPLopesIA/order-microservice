package com.rodrigolopes.inventory_service.dto;

import lombok.Builder;

@Builder
public record ItemResponseDTO(String id, String name, String description, double price, int quantity, long createdAt, long updatedAt) {
}
