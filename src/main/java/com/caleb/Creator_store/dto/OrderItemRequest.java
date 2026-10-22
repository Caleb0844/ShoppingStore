package com.caleb.Creator_store.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder 
public class OrderItemRequest {
    @NotNull (message = "Product ID is required")
    private Long productId;

    @NotNull (message = "Product qquantity is required")
    @Min (value = 1, message = "Quantity must be atleast 1")
    private Integer quantity;
    
}
