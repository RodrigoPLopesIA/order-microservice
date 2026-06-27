package com.rodrigolopes.inventory_service.consumers;

import com.rodrigolopes.inventory_service.dto.OrderDTO;
import com.rodrigolopes.inventory_service.services.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class ItemConsumer {

    private final ObjectMapper objectMapper;
    private final ItemService itemService;

    public ItemConsumer(ItemService itemService) {
        this.objectMapper = new ObjectMapper();
        this.itemService = itemService;
    }
    @KafkaListener(topics = "create-order-topic")
    public void consumer(String message) {

        try {
            log.info("Received message: {}", message);
            this.itemService.verifyInventory(objectMapper.readValue(message, OrderDTO.class));
        }catch (Exception e) {
            log.warn(e.getMessage());
        }

    }
}
