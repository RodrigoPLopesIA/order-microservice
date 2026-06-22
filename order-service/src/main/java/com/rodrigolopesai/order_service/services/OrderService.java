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
        var totalItems = getTotalItems(data);
        var totalPrice = getTotalPrice(data);
        var items = data.items().stream().map(item -> Item.builder()
                .name(item.name())
                .quantity(item.quantity())
                .price(item.price())
                .build()).toList();
        
        Order order = Order.builder().totalItems(totalItems).totalPrice(totalPrice).items(items).build();
        var saved = orderRepository.save(order);

        List<ItemDTO> itemsDTO = saved.getItems().stream().map(item -> com.rodrigolopesai.order_service.dto.ItemDTO.builder()
                .name(item.getName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build()).toList();

        var response = OrderDTO.builder().id(saved.getId()).items(itemsDTO).totalItems(saved.getTotalItems()).totalPrice(saved.getTotalPrice())
                .createdAt(saved.getCreatedAt()).updatedAt(saved.getUpdatedAt()).build();

        producerService.sendMessage("create-order-topic", response);
        return response;
    }

    private int getTotalItems(CreateOrderDTO data) {
        return data.items().stream().mapToInt(ItemDTO::quantity).sum();
    }

    private double getTotalPrice(CreateOrderDTO data) {
        return data.items().stream().mapToDouble(item -> item.quantity() * item.price()).sum();
    }


}
