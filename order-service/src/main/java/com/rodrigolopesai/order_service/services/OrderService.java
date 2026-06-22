package com.rodrigolopesai.order_service.services;

import com.rodrigolopesai.order_service.dto.CreateOrderDTO;
import com.rodrigolopesai.order_service.dto.ItemDTO;
import com.rodrigolopesai.order_service.dto.OrderDTO;
import com.rodrigolopesai.order_service.entities.Item;
import com.rodrigolopesai.order_service.entities.Order;
import com.rodrigolopesai.order_service.repositories.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {


    private final OrderRepository orderRepository;
    private final ProducerService producerService;


    public OrderService(OrderRepository orderRepository, ProducerService producerService) {
        this.orderRepository = orderRepository;
        this.producerService = producerService;
    }

    public OrderDTO save(CreateOrderDTO data) {
        var order = mapToEntity(data);
        var saved = orderRepository.save(order);
        var response = mapToDTO(saved);
        producerService.sendMessage("create-order-topic", response);

        return response;
    }

    private int getTotalItems(CreateOrderDTO data) {
        return data.items().stream().mapToInt(ItemDTO::quantity).sum();
    }

    private double getTotalPrice(CreateOrderDTO data) {
        return data.items().stream().mapToDouble(item -> item.quantity() * item.price()).sum();
    }
    private Order mapToEntity(CreateOrderDTO data) {
        var totalItems = getTotalItems(data);
        var totalPrice = getTotalPrice(data);
        var items = data.items().stream().map(item -> Item.builder()
                .name(item.name())
                .quantity(item.quantity())
                .price(item.price())
                .build()).toList();

        Order order = Order.builder().totalItems(totalItems).totalPrice(totalPrice).items(items).build();
        return order;
    }
    private OrderDTO mapToDTO(Order order) {
        List<ItemDTO> itemsDTO = order.getItems().stream().map(item -> ItemDTO.builder()
                .name(item.getName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build()).toList();

        return OrderDTO.builder().id(order.getId()).items(itemsDTO).totalItems(order.getTotalItems()).totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt()).updatedAt(order.getUpdatedAt()).build();
    }


}
