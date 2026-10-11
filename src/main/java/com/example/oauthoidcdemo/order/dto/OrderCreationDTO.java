package com.example.oauthoidcdemo.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OrderCreationDTO(
        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 120, message = "A descrição deve ter no máximo 120 caracteres.")
        String description,
        @NotNull(message = "O valor é obrigatório.")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
        @Digits(integer = 10, fraction = 2, message = "O valor deve ter no máximo 10 dígitos inteiros e 2 casas decimais.")
        BigDecimal amount
) {
}
