package com.example.oauthoidcdemo.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponseDTO(
        Long id,
        String description,
        BigDecimal amount,
        Instant createdAt
) {
}
