package com.rodrigolopes.inventory_service.services;

import com.rodrigolopes.inventory_service.dto.ItemDTO;
import com.rodrigolopes.inventory_service.dto.OrderDTO;
import com.rodrigolopes.inventory_service.dto.ItemRequestDTO;
import com.rodrigolopes.inventory_service.dto.ItemResponseDTO;
import com.rodrigolopes.inventory_service.entities.Item;
import com.rodrigolopes.inventory_service.enums.OrderStatus;
import com.rodrigolopes.inventory_service.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
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

    public void verifyInventory(OrderDTO order) {
        try {
            // somente processar se o status for CREATED ou PROCESSING
            boolean isCreated = "CREATED".equalsIgnoreCase(order.status().toString());
            boolean isProcessing = "PROCESSING".equalsIgnoreCase(order.status().toString());
            if (!isCreated && !isProcessing) {
                return;
            }

            // se estiver CREATED, enviar evento para atualizar para PROCESSING
            if (isCreated) {
                producerService.sendMessage("update-order-status",
                        Map.of("orderId", order.id(), "status", OrderStatus.PROCESSING));
            }

            // verificar cada item
            for (ItemDTO itemDto : order.items()) {
                Optional<Item> optItem = itemRepository.findById(itemDto.itemId());
                if (optItem.isEmpty()) {
                    log.warn("Item not found: " + itemDto.itemId());
                    producerService.sendMessage("update-order-status",
                            Map.of(
                                    "orderId", order.id(),
                                    "status", OrderStatus.FAILED,
                                    "reason", "ITEM_NOT_FOUND",
                                    "itemId", itemDto.itemId()
                            ));
                    return;
                }

                Item item = optItem.get();

                if (itemDto.quantity() > item.getQuantity()) {
                    log.warn("Requested quantity greater than available for item: " + itemDto.itemId());
                    producerService.sendMessage("update-order-status",
                            Map.of(
                                    "orderId", order.id(),
                                    "status", OrderStatus.FAILED,
                                    "reason", "QUANTITY_EXCEEDED",
                                    "itemId", itemDto.itemId(),
                                    "requested", itemDto.quantity(),
                                    "available", item.getQuantity()
                            ));
                    return;
                }

                // decrementar quantidade e salvar
                item.setQuantity(item.getQuantity() - itemDto.quantity());
                itemRepository.save(item);
            }

            // se todos os items existem e quantidades foram atualizadas
            producerService.sendMessage("update-order-status",
                    Map.of("orderId", order.id(), "status", "COMPLETED"));

        } catch (Exception e) {
            log.warn("Error verifying inventory for order: " + order.id() + " - " + e.getMessage(), e);
        }
    }

    public ItemResponseDTO getById(String itemId) {
        var item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
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

        return new ItemResponseDTO(savedProduct.getItemId().toString(), savedProduct.getName(), savedProduct.getDescription(), savedProduct.getPrice(), savedProduct.getQuantity(), savedProduct.getCreatedAt(), savedProduct.getUpdatedAt());

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
    }
}
