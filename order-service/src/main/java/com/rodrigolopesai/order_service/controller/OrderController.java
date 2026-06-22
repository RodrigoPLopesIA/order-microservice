package com.rodrigolopesai.order_service.controller;


import com.rodrigolopesai.order_service.dto.CreateOrderDTO;
import com.rodrigolopesai.order_service.dto.OrderDTO;
import com.rodrigolopesai.order_service.services.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/orders")
@RestController
public class OrderController {


    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    @PostMapping
    public OrderDTO createOrder(@RequestBody CreateOrderDTO data) {
        return this.orderService.save(data);
    }
}
