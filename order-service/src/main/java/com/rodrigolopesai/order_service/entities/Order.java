package com.rodrigolopesai.order_service.entities;


import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@Document(collection = "orders")
public class Order {

    @Id
    private String id;
    private List<Item> items = new ArrayList<>();
    private int totalItems;
    private double totalPrice;

    @CreatedDate
    private Long createdAt;
    @LastModifiedDate
    private Long updatedAt;


}
