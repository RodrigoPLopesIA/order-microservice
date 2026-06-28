package com.rodrigolopes.inventory_service.entities;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Data
@Document(collection = "items")
@Builder
public class Item {

    @Id
    private String id;
    private String name;
    @Indexed(unique = true)
    private String itemId;
    private String description;
    private int quantity;
    private double price;

    @CreatedDate
    private long createdAt;
    @LastModifiedDate
    private long updatedAt;
}
