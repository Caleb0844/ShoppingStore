package com.caleb.Creator_store.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.caleb.Creator_store.Entities.OrderItem;


public interface OrderItemRepo extends JpaRepository<OrderItem, Long > {

    // Returns only order lines belonging to products owned by the authenticated seller.
    List<OrderItem> findAllByProductSellerProfileUserId(Long userId);

    // Confirms a seller owns at least one line before allowing fulfillment changes.
    boolean existsByOrderIdAndProductSellerProfileUserId(Long orderId, Long userId);

}
