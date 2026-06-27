package com.rodrigolopesai.order_service.dto;

import com.rodrigolopesai.order_service.enums.OrderStatus;
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
