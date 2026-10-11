package com.example.oauthoidcdemo.order.service;

import com.example.oauthoidcdemo.order.dto.OrderCreationDTO;
import com.example.oauthoidcdemo.order.dto.OrderResponseDTO;

import java.util.List;

public interface OrderService {

    List<OrderResponseDTO> findOwnOrders(String ownerSubject);

    OrderResponseDTO create(OrderCreationDTO orderCreationDTO, String ownerSubject);

    void deleteById(Long orderId);
}
