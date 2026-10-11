package com.example.oauthoidcdemo.order.service.impl;

import com.example.oauthoidcdemo.common.exception.NotFoundException;
import com.example.oauthoidcdemo.order.dto.OrderCreationDTO;
import com.example.oauthoidcdemo.order.dto.OrderResponseDTO;
import com.example.oauthoidcdemo.order.entity.Order;
import com.example.oauthoidcdemo.order.mapper.OrderMapper;
import com.example.oauthoidcdemo.order.repository.OrderRepository;
import com.example.oauthoidcdemo.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public List<OrderResponseDTO> findOwnOrders(String ownerSubject) {
        return orderRepository.findAllByOwnerSubjectOrderByCreatedAtDesc(ownerSubject)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponseDTO create(OrderCreationDTO orderCreationDTO, String ownerSubject) {
        Order order = Order.builder()
                .description(orderCreationDTO.description())
                .amount(orderCreationDTO.amount())
                .ownerSubject(ownerSubject)
                .createdAt(Instant.now())
                .build();

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void deleteById(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new NotFoundException("Pedido não encontrado.");
        }
        orderRepository.deleteById(orderId);
    }
}
