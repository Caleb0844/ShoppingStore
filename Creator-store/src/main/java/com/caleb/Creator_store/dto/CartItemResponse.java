package com.caleb.Creator_store.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

// Defines a safe cart-line response using current catalog information.
@Value
@Builder
public class CartItemResponse {

    Long productId;
    String productName;
    Long sellerProfileId;
    Integer quantity;
    BigDecimal currentPrice;
    BigDecimal lineTotal;
}
