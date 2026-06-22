package com.rodrigolopesai.order_service.dto;


import lombok.Builder;

import java.util.List;

@Builder
public record CreateOrderDTO(
        List<ItemDTO> items){
}
