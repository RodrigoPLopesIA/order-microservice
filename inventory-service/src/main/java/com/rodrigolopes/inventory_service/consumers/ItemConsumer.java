package com.rodrigolopes.inventory_service.consumers;

import com.rodrigolopes.inventory_service.dto.OrderDTO;
import com.rodrigolopes.inventory_service.enums.OrderStatus;
import com.rodrigolopes.inventory_service.exceptions.ItemNotFoundException;
import com.rodrigolopes.inventory_service.exceptions.OutOfStockException;
import com.rodrigolopes.inventory_service.services.ItemService;
import com.rodrigolopes.inventory_service.services.ProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@Slf4j
public class ItemConsumer {

    private final ObjectMapper objectMapper;
    private final ItemService itemService;
    private final ProducerService producerService;
    private static final String UPDATE_ORDER_STATUS_TOPIC = "update-order-status";

    public ItemConsumer(ItemService itemService, ProducerService producerService) {
        this.objectMapper = new ObjectMapper();
        this.itemService = itemService;
        this.producerService = producerService;
    }
    private void publishMessage(String topic, Map<String, Object> message) {
        producerService.sendMessage(topic, message);
    }

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(
                    delay = 1000,
                    multiplier = 2.0
            ),
            dltTopicSuffix = "-dlt"

    )
    @KafkaListener(topics = "create-order-topic")
    public void consumer(String message) throws Exception {

        OrderDTO order = objectMapper.readValue(message, OrderDTO.class);

        try {
            itemService.processOrder(order);

        } catch (ItemNotFoundException ex) {

            publishMessage(
                    UPDATE_ORDER_STATUS_TOPIC,
                    Map.of(
                            "orderId", ex.getOrderId(),
                            "status", OrderStatus.FAILED,
                            "reason", "ITEM_NOT_FOUND",
                            "itemId", ex.getItemId()
                    )
            );

        } catch (OutOfStockException ex) {

            publishMessage(
                    UPDATE_ORDER_STATUS_TOPIC,
                    Map.of(
                            "orderId", ex.getOrderId(),
                            "status", OrderStatus.FAILED,
                            "reason", "QUANTITY_EXCEEDED",
                            "itemId", ex.getItemId(),
                            "requested", ex.getRequested(),
                            "available", ex.getAvailable()
                    )
            );
        }
    }

    @DltHandler
    public void processOrderDlt(String message) {
        log.warn("Message sent to DLT: {}", message);
    }


}
