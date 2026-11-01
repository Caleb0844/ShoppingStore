package com.caleb.Creator_store.Entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.caleb.Creator_store.Enums.FulfillmentStatus;
import com.caleb.Creator_store.Enums.OrderStatus;
import com.caleb.Creator_store.Enums.PaymentStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Stores one customer order and its immutable purchase lines.
// The customer relationship prevents clients from assigning an order to another user.
@Entity
@Table(name = "orders")
@Getter 
@Setter
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 

public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @NotBlank (message = "Customer name is required")
    @Column (nullable = false)
    private String  customer_name;

    @NotBlank (message = "Customer email is required")
    @Column (nullable = false)
    private String customer_email; 

    // Legacy status is retained during schema migration; new code uses the typed status fields below.
    @Deprecated
    @NotBlank(message = "Order status cant be blank")
    @Column(nullable = false)
    private String status;

    // Keeps order lifecycle decisions separate from payment and delivery state.
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    // Tracks payment independently so M-Pesa failures do not incorrectly cancel fulfillment state.
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // Tracks seller fulfillment progress independently from order and payment state.
    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false)
    @Builder.Default
    private FulfillmentStatus fulfillmentStatus = FulfillmentStatus.PENDING;

    @JsonManagedReference 
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
    


    @NotNull (message = "Total amount must be valid")
    @DecimalMin(value = "0.0", inclusive = false, message = "total amount must be greater than zero")
    @Column (nullable = false)
    private BigDecimal total_amount;



    @Column (name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist 
    public void prePersist(){
        if(createdAt == null){
            createdAt = LocalDateTime.now();
        }
        if(status == null || status.isBlank()){
            status = "PENDING";
        }
        if (orderStatus == null) {
            orderStatus = OrderStatus.PENDING;
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
        if (fulfillmentStatus == null) {
            fulfillmentStatus = FulfillmentStatus.PENDING;
        }
    }

}
