package com.caleb.Creator_store.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

// Defines the order-line data a seller needs to fulfill their own product sales.
// Customer identity and other sellers' lines are deliberately excluded.
@Value
@Builder
public class SellerOrderItemResponse {

    Long productId;
    String productName;
    Integer quantity;
    BigDecimal priceAtPurchase;
    BigDecimal lineTotal;
}
