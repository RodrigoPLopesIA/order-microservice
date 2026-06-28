package com.rodrigolopes.inventory_service.controller;

import com.rodrigolopes.inventory_service.dto.ItemRequestDTO;
import com.rodrigolopes.inventory_service.dto.ItemResponseDTO;
import com.rodrigolopes.inventory_service.services.ItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{itemId}")
    public ResponseEntity<ItemResponseDTO> updateItem(@PathVariable String itemId, @Valid @RequestBody ItemRequestDTO itemRequestDTO) {
        var updated = itemService.update(itemId, itemRequestDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDTO> getItem(@PathVariable String itemId) {
        var item = itemService.getById(itemId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<Page<ItemResponseDTO>> getAllItems(Pageable pageable) {
        var items = itemService.findAll(pageable);
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable String itemId) {
        itemService.delete(itemId);
        return ResponseEntity.noContent().build();
    }

}
