package com.rodrigolopes.inventory_service.repository;

import com.rodrigolopes.inventory_service.entities.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ItemRepository extends MongoRepository<Item, String> {

    Optional<Item> findByName(String name);

    Optional<Item> findByItemId(String itemId);
}
