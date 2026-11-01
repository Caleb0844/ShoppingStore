package com.caleb.Creator_store.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

// Defines the safe public product representation without exposing seller internals or order relationships.
@Value
@Builder
public class ProductResponse {

    Long id;
    String name;
    String description;
    String category;
    BigDecimal price;
    Integer stockQuantity;
    boolean active;
    Long sellerProfileId;
}
