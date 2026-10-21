package com.caleb.Creator_store.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caleb.Creator_store.Entities.Product;


public interface ProductRepo extends JpaRepository<Product, Long>{

}
