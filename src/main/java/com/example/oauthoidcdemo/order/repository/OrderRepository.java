package com.example.oauthoidcdemo.order.repository;

import com.example.oauthoidcdemo.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOwnerSubjectOrderByCreatedAtDesc(String ownerSubject);
}
