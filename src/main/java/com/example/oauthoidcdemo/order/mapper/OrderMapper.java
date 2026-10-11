package com.example.oauthoidcdemo.order.mapper;

import com.example.oauthoidcdemo.order.dto.OrderResponseDTO;
import com.example.oauthoidcdemo.order.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {

    OrderResponseDTO toResponse(Order order);
}
