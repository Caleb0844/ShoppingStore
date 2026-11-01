package com.caleb.Creator_store.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Value;
import com.caleb.Creator_store.Enums.FulfillmentStatus;
import com.caleb.Creator_store.Enums.OrderStatus;
import com.caleb.Creator_store.Enums.PaymentStatus;

// Defines the safe order response used by customer order-history endpoints.
@Value
@Builder
public class OrderResponse {

    Long id;
    String customerName;
    String customerEmail;
    String status;
    OrderStatus orderStatus;
    PaymentStatus paymentStatus;
    FulfillmentStatus fulfillmentStatus;
    BigDecimal totalAmount;
    LocalDateTime createdAt;
    List<OrderItemResponse> items;
}
