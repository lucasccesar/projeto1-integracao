package com.example.oauthoidcdemo.order;

import com.example.oauthoidcdemo.common.dto.PublicResponseDTO;
import com.example.oauthoidcdemo.order.dto.OrderCreationDTO;
import com.example.oauthoidcdemo.order.dto.OrderResponseDTO;
import com.example.oauthoidcdemo.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/public")
    public PublicResponseDTO publicEndpoint() {
        return new PublicResponseDTO("Endpoint público acessível sem token.");
    }

    @GetMapping("/orders")
    public List<OrderResponseDTO> findOwnOrders(Authentication authentication) {
        return orderService.findOwnOrders(authentication.getName());
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponseDTO> create(
            @Valid @RequestBody OrderCreationDTO orderCreationDTO,
            Authentication authentication
    ) {
        OrderResponseDTO response = orderService.create(orderCreationDTO, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "O identificador deve ser positivo.") Long orderId
    ) {
        orderService.deleteById(orderId);
        return ResponseEntity.noContent().build();
    }
}
