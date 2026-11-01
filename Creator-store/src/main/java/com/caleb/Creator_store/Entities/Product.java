package com.caleb.Creator_store.Entities;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

// Stores a catalog item owned by one approved seller profile.
// Product ownership is persisted here so update/archive authorization remains enforceable.
@Entity
@Table(name = "products")
@Getter 
@Setter 
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;
    
    @Column
    private String description;

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;

    @NotNull(message = "Price is required")
    @DecimalMin (value = "0.0",inclusive = false, message = "Price must be greater than zero")
    @Column(nullable = false)
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be less than 0")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    // Keeps historical product records available while controlling whether the item is currently sellable.
    // Archive instead of deleting products so future orders can retain their product relationship.
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "seller_profile_id", nullable = false)
    private SellerProfile sellerProfile;

    @JsonIgnore
    @OneToMany(mappedBy = "product") 
    private List<OrderItem> orderItems;


}
