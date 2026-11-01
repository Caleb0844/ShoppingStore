package com.caleb.Creator_store.dto;


import java.util.List;

import jakarta.validation.Valid;
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

    @Valid @NotEmpty (message = "Enter a minimum of one order")
    private List<OrderItemRequest> items;
}
