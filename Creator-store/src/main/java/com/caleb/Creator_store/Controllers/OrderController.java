package com.caleb.Creator_store.Controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caleb.Creator_store.Services.OrderService;
import com.caleb.Creator_store.dto.OrderRequest;
import com.caleb.Creator_store.dto.OrderResponse;
import com.caleb.Creator_store.dto.SellerOrderResponse;
import com.caleb.Creator_store.dto.FulfillmentStatusUpdateRequest;
import com.caleb.Creator_store.security.CurrentUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Exposes authenticated customer checkout and customer-owned order history.
// The customer identity always comes from the verified JWT, never from request data.
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    // Returns only order lines belonging to products owned by the authenticated seller.
    @GetMapping("/seller")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SELLER')")
    public List<SellerOrderResponse> getSellerOrders() {
        return orderService.getOrdersForSeller(
                currentUserService.getCurrentUser().userId());
    }

    // Checks the authenticated customer's cart and creates one order transactionally.
    @PostMapping("/checkout")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> checkoutCart() {
        OrderResponse response = orderService.checkoutCart(
                currentUserService.getCurrentUser().userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Creates an order for the authenticated customer.
    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        OrderResponse response = orderService.createOrder(
                currentUserService.getCurrentUser().userId(), orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Returns only orders owned by the authenticated customer.
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')")
    public List<OrderResponse> getOrders() {
        return orderService.getOrdersForCustomer(currentUserService.getCurrentUser().userId());
    }

    // Returns one order only when it belongs to the authenticated customer.
    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderForCustomer(
                currentUserService.getCurrentUser().userId(), id);
    }

    // Cancels an owned order while it is still pending or confirmed.
    @PostMapping("/{id}/cancel")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(
                currentUserService.getCurrentUser().userId(), id);
    }

    // Allows a seller to advance fulfillment only for an order containing their product.
    @PatchMapping("/{id}/fulfillment")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SELLER')")
    public SellerOrderResponse updateFulfillmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody FulfillmentStatusUpdateRequest request) {
        return orderService.updateFulfillmentStatus(
                currentUserService.getCurrentUser().userId(), id, request);
    }
}
