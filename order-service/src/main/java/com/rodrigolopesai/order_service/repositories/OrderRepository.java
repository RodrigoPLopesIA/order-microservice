package com.rodrigolopesai.order_service.repositories;

import com.rodrigolopesai.order_service.entities.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
}
