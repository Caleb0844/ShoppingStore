package com.caleb.Creator_store.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.caleb.Creator_store.Entities.OrderItem;


public interface OrderItemRepo extends JpaRepository<OrderItem, Long > {

}
