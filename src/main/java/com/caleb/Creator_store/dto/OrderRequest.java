package com.caleb.Creator_store.dto;


import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 

public class OrderRequest {

    @NotBlank (message = "Customer name required")
    private String customerName;
    @NotBlank (message = "Customer Email is required")
    @Email (message = "Enter a valid email address")
    private String customerEmail;

    @Valid @NotEmpty (message = "Enter a minimum of one order")
    private List<OrderItemRequest> items;
}
