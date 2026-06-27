package com.rodrigolopes.inventory_service.controller;

import com.rodrigolopes.inventory_service.dto.ItemRequestDTO;
import com.rodrigolopes.inventory_service.dto.ItemResponseDTO;
import com.rodrigolopes.inventory_service.services.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }
    @PostMapping
    public ResponseEntity<ItemResponseDTO> createItem(@Valid @RequestBody ItemRequestDTO itemRequestDTO) {
        var saved = itemService.create(itemRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
