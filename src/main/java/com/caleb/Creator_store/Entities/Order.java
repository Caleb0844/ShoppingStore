package com.caleb.Creator_store.Entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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

@Entity
@Table(name = "orders")
@Getter 
@Setter
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 

public class Order {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank (message = "Customer name is required")
    @Column (nullable = false)
    private String  customer_name;

    @NotBlank (message = "Customer email is required")
    @Column (nullable = false)
    private String customer_email; 

    @NotBlank (message = "Order status cant be blank")
    @Column (nullable = false)
    private String status;

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
    }

}


