package com.caleb.Creator_store.dto;

import com.caleb.Creator_store.Enums.FulfillmentStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Defines the only fulfillment input accepted from a seller.
// The service still validates ownership and the allowed state transition.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FulfillmentStatusUpdateRequest {

    @NotNull(message = "Fulfillment status is required")
    private FulfillmentStatus status;
}
