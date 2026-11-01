package com.caleb.Creator_store.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Value;
import com.caleb.Creator_store.Enums.FulfillmentStatus;
import com.caleb.Creator_store.Enums.OrderStatus;
import com.caleb.Creator_store.Enums.PaymentStatus;

// Defines the seller-scoped order view without exposing customer personal data.
@Value
@Builder
public class SellerOrderResponse {

    Long orderId;
    String status;
    OrderStatus orderStatus;
    PaymentStatus paymentStatus;
    FulfillmentStatus fulfillmentStatus;
    LocalDateTime createdAt;
    BigDecimal sellerSubtotal;
    List<SellerOrderItemResponse> items;
}
