package com.caleb.Creator_store.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

// Defines the safe purchase-line response without serializing product or order entities.
@Value
@Builder
public class OrderItemResponse {

    Long productId;
    String productName;
    Long sellerProfileId;
    Integer quantity;
    BigDecimal priceAtPurchase;
}
