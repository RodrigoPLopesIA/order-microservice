package com.rodrigolopesai.order_service.dto;

import lombok.Builder;

@Builder
public record ItemDTO(String name, int quantity, double price) {
}
