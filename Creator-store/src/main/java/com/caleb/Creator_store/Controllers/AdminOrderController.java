package com.caleb.Creator_store.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caleb.Creator_store.Services.OrderService;
import com.caleb.Creator_store.dto.OrderResponse;

import lombok.RequiredArgsConstructor;

// Exposes complete order oversight only to administrators.
// Keep this separate from customer and seller views so broader data access is explicit.
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    // Returns all orders for administrative oversight.
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrdersForAdmin();
    }

    // Returns one complete order for an administrator.
    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrderForAdmin(id);
    }
}
