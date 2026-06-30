package com.rodrigolopes.inventory_service.services;

import com.rodrigolopes.inventory_service.dto.ItemDTO;
import com.rodrigolopes.inventory_service.dto.OrderDTO;
import com.rodrigolopes.inventory_service.dto.ItemRequestDTO;
import com.rodrigolopes.inventory_service.dto.ItemResponseDTO;
import com.rodrigolopes.inventory_service.entities.Item;
import com.rodrigolopes.inventory_service.enums.OrderStatus;
import com.rodrigolopes.inventory_service.exceptions.ItemNotFoundException;
import com.rodrigolopes.inventory_service.exceptions.OutOfStockException;
import com.rodrigolopes.inventory_service.producers.ProducerService;
import com.rodrigolopes.inventory_service.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ProducerService producerService;

    public ItemService(ItemRepository itemRepository, ProducerService producerService) {
        this.itemRepository = itemRepository;
        this.producerService = producerService;
    }
    private boolean isCreatedStatus(String status) {
        return "CREATED".equalsIgnoreCase(status);
    }

    private boolean isProcessingStatus(String status) {
        return "PROCESSING".equalsIgnoreCase(status);
    }

    private boolean isValidStatus(String status) {
        return isCreatedStatus(status) || isProcessingStatus(status);
    }

    private void publishMessage(String topic, Map<String, Object> message) {
        producerService.sendMessage(topic, message);
    }
    private static final String UPDATE_ORDER_STATUS_TOPIC = "update-order-status";

    public void processOrder(OrderDTO order) {

        if (!isValidStatus(order.status().name())) {
            log.warn("Ignoring order {} because status is {}", order.id(), order.status());
            return;
        }

        if (isCreatedStatus(order.status().name())) {
            publishProcessingStatus(order.id());
        }

        for (ItemDTO itemDto : order.items()) {
            processItem(order, itemDto);
        }

        publishCompletedStatus(order.id());
    }

    private void processItem(OrderDTO order, ItemDTO itemDto) {

        Item item = itemRepository.findByItemId(itemDto.itemId())
                .orElseThrow(() -> new ItemNotFoundException(order.id(), itemDto.itemId()));

        validateStock(item, itemDto, order.id());

        item.setQuantity(item.getQuantity() - itemDto.quantity());

        itemRepository.save(item);
    }

    private void validateStock(Item item, ItemDTO itemDto, String orderId) {

        if (itemDto.quantity() > item.getQuantity()) {
            throw new OutOfStockException(
                    orderId,
                    itemDto.itemId(),
                    itemDto.quantity(),
                    item.getQuantity()
            );
        }
    }

    private void publishProcessingStatus(String orderId) {
        publishMessage(
                UPDATE_ORDER_STATUS_TOPIC,
                Map.of(
                        "orderId", orderId,
                        "status", OrderStatus.PROCESSING
                )
        );
    }

    private void publishCompletedStatus(String orderId) {
        publishMessage(
                UPDATE_ORDER_STATUS_TOPIC,
                Map.of(
                        "orderId", orderId,
                        "status", OrderStatus.COMPLETED
                )
        );
    }

    public ItemResponseDTO getById(String itemId) {
        var item = itemRepository.findByItemId(itemId).orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        return new ItemResponseDTO(item.getItemId(), item.getName(), item.getDescription(), item.getPrice(), item.getQuantity(), item.getCreatedAt(), item.getUpdatedAt());
    }

    public Page<ItemResponseDTO> findAll(Pageable pageable) {
        var itemsPage = itemRepository.findAll(pageable);
        return itemsPage.map(item -> new ItemResponseDTO(item.getItemId(), item.getName(), item.getDescription(), item.getPrice(), item.getQuantity(), item.getCreatedAt(), item.getUpdatedAt()));
    }

    public ItemResponseDTO create(ItemRequestDTO data){

        var product = Item.builder().itemId(UUID.randomUUID().toString()).name(data.name()).price(data.price()).quantity(data.quantity()).description(data.description()).build();

        itemRepository.findByName(data.name()).ifPresent(existingProduct -> {
            throw new RuntimeException("Product with name " + data.name() + " already exists.");
        });
        var savedProduct = itemRepository.save(product);

        return new ItemResponseDTO(savedProduct.getItemId(), savedProduct.getName(), savedProduct.getDescription(), savedProduct.getPrice(), savedProduct.getQuantity(), savedProduct.getCreatedAt(), savedProduct.getUpdatedAt());

    }

    public ItemResponseDTO update(String itemId, ItemRequestDTO data) {
        var item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        item.setName(data.name());
        item.setDescription(data.description());
        item.setPrice(data.price());
        item.setQuantity(data.quantity());
        var updatedItem = itemRepository.save(item);
        return new ItemResponseDTO(updatedItem.getItemId(), updatedItem.getName(), updatedItem.getDescription(), updatedItem.getPrice(), updatedItem.getQuantity(), updatedItem.getCreatedAt(), updatedItem.getUpdatedAt());
    }

    public void delete(String itemId) {
        var item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        itemRepository.delete(item);

        publishMessage("delete-item-order-topic", Map.of("itemId", item.getItemId()));
    }
}
