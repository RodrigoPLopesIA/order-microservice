package com.rodrigolopes.inventory_service.repository;

import com.rodrigolopes.inventory_service.entities.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemRepository extends MongoRepository<Item, String> {
}
