package com.caleb.Creator_store.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caleb.Creator_store.Entities.Order;

public interface OrderRepo extends JpaRepository<Order, Long > {

}
