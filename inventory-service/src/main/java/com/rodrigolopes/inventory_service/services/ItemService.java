package com.rodrigolopes.inventory_service.services;

import com.rodrigolopes.inventory_service.dto.ItemDTO;
import com.rodrigolopes.inventory_service.dto.OrderDTO;
import com.rodrigolopes.inventory_service.entities.Item;
import com.rodrigolopes.inventory_service.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

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
            boolean isCreated = "CREATED".equals(order.status());
            boolean isProcessing = "PROCESSING".equals(order.status());
            if (!isCreated && !isProcessing) {
                return;
            }

            // se estiver CREATED, enviar evento para atualizar para PROCESSING
            if (isCreated) {
                producerService.sendMessage("update-order-status",
                        Map.of("orderId", order.id(), "status", "PROCESSING"));
            }

            // verificar cada item
            for (ItemDTO itemDto : order.items()) {
                Optional<Item> optItem = itemRepository.findById(itemDto.itemId());
                if (optItem.isEmpty()) {
                    log.warn("Item not found: " + itemDto.itemId());
                    producerService.sendMessage("update-order-status",
                            Map.of(
                                    "orderId", order.id(),
                                    "status", "CANCELED",
                                    "reason", "ITEM_NOT_FOUND",
                                    "itemId", itemDto.itemId()
                            ));
                    return;
                }

                Item item = optItem.get();

                // verificar quantidade
                if (itemDto.quantity() > item.getQuantity()) {
                    log.warn("Requested quantity greater than available for item: " + itemDto.itemId());
                    producerService.sendMessage("update-order-status",
                            Map.of(
                                    "orderId", order.id(),
                                    "status", "CANCELED",
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
}
