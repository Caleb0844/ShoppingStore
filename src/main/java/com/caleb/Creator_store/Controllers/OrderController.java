
package com.caleb.Creator_store.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caleb.Creator_store.Entities.Order;
import com.caleb.Creator_store.Services.OrderService;
import com.caleb.Creator_store.dto.OrderRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

@PostMapping
public Order createOrder(@Valid @RequestBody OrderRequest orderRequest) {
    return orderService.createOrder(orderRequest);
}



    @GetMapping 
    public List<Order> getAllOrders() {
    return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
    return orderService.getOrderById(id);
}
}


