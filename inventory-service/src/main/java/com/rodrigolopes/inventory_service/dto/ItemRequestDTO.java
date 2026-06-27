package com.rodrigolopes.inventory_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record ItemRequestDTO(@NotBlank String name, String description, @Positive double price, @Positive @Min(value = 1) int quantity) {
}
