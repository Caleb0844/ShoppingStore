package com.caleb.Creator_store.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caleb.Creator_store.Entities.Order;

public interface OrderRepo extends JpaRepository<Order, Long > {

    // Restricts customer order history to orders owned by the authenticated user.
    List<Order> findAllByCustomerId(Long userId);

    // Prevents a customer from reading another customer's order by ID.
    Optional<Order> findByIdAndCustomerId(Long orderId, Long userId);

}
