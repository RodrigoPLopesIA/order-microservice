package com.rodrigolopesai.order_service.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Item {


    String name;
    int quantity;
    double price;

}
