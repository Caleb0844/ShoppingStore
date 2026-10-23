package com.caleb.Creator_store.Services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.caleb.Creator_store.Entities.Order;
import com.caleb.Creator_store.Entities.OrderItem;
import com.caleb.Creator_store.Entities.Product;
import com.caleb.Creator_store.Repositories.OrderRepo;
import com.caleb.Creator_store.Repositories.ProductRepo;
import com.caleb.Creator_store.dto.OrderItemRequest;
import com.caleb.Creator_store.dto.OrderRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service 
@RequiredArgsConstructor 
public class OrderService {
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;

    @Transactional 
    public Order createOrder(OrderRequest orderRequest){
        List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal totalPrice = BigDecimal.ZERO;

        Order order = new Order();
        order.setCustomer_name(orderRequest.getCustomerName());
        order.setCustomer_email(orderRequest.getCustomerEmail());
        order.setStatus("CONFIRMED");

        for(OrderItemRequest itemRequest:orderRequest.getItems()){
            Product product = productRepo.findById(
                itemRequest.getProductId()
            )
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemRequest.getProductId()));

            if(product.getStockQuantity() < itemRequest.getQuantity() ){
                throw new RuntimeException("Not enough quantity for this product");
            }
            //Calculate total price
            BigDecimal priceOfItems = product.getPrice()
                .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            totalPrice = totalPrice.add(priceOfItems);

            //upate stock quantity
            product.setStockQuantity(
                product.getStockQuantity() - itemRequest.getQuantity()
            );
            productRepo.save(product);

            //BuilderPattern to make obj
            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(itemRequest.getQuantity())
                .priceAtPurchase(product.getPrice())
                .build();

                orderItems.add(orderItem);

        }
        order.setOrderItems(orderItems);
        order.setTotal_amount(totalPrice);
        return orderRepo.save(order);
    }

    



}
