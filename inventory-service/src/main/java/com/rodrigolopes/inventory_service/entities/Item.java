package com.rodrigolopes.inventory_service.entities;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "items")
public class Item {

    @Id
    private String id;
    private String name;
    private String description;
    private int quantity;
    private double price;

    @CreatedDate
    private long createdAt;
    @LastModifiedDate
    private long updatedAt;
}
