package com.rodrigolopes.inventory_service.dto;

import com.rodrigolopes.inventory_service.enums.OrderStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record OrderDTO(
        String id,
        List<ItemDTO> items,
        int totalItems,
        double totalPrice,
        OrderStatus status,
        Long createdAt,
        Long updatedAt){
}
