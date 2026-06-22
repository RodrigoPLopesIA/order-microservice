package com.rodrigolopesai.order_service.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderDTO(
        String id,
        List<ItemDTO> items,
        int totalItems,
        double totalPrice,
        Long createdAt,
        Long updatedAt){
}
