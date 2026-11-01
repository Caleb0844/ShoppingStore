package com.caleb.Creator_store.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Value;

// Defines the safe customer cart response without exposing JPA entities or relationships.
@Value
@Builder
public class CartResponse {

    Long id;
    List<CartItemResponse> items;
    BigDecimal total;
}
