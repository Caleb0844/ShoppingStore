package com.caleb.Creator_store.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.caleb.Creator_store.Entities.Order;
import com.caleb.Creator_store.Entities.Product;
import com.caleb.Creator_store.Entities.Cart;
import com.caleb.Creator_store.Entities.CartItem;
import com.caleb.Creator_store.Entities.OrderItem;
import com.caleb.Creator_store.Entities.SellerProfile;
import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.Enums.AuditAction;
import com.caleb.Creator_store.Enums.SellerApplicationStatus;
import com.caleb.Creator_store.Enums.FulfillmentStatus;
import com.caleb.Creator_store.Enums.OrderStatus;
import com.caleb.Creator_store.Enums.PaymentStatus;
import com.caleb.Creator_store.Repositories.OrderRepo;
import com.caleb.Creator_store.Repositories.ProductRepo;
import com.caleb.Creator_store.Repositories.UserRepo;
import com.caleb.Creator_store.Repositories.CartRepo;
import com.caleb.Creator_store.Repositories.OrderItemRepo;
import com.caleb.Creator_store.dto.OrderItemRequest;
import com.caleb.Creator_store.dto.OrderRequest;
import com.caleb.Creator_store.dto.OrderResponse;
import com.caleb.Creator_store.dto.FulfillmentStatusUpdateRequest;
import com.caleb.Creator_store.exceptions.BusinessRuleException;
import com.caleb.Creator_store.exceptions.ResourceNotFoundException;

// Verifies authenticated checkout rules without requiring a database or HTTP server.
// Reuse this pattern for stock, price, and ownership rules in future checkout features.
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private CartRepo cartRepo;

    @Mock
    private OrderItemRepo orderItemRepo;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private OrderService orderService;

    // Confirms checkout uses the database price, decrements stock, and binds the order to the customer.
    @Test
    void createOrderUsesAuthenticatedCustomerAndAuthoritativeProductData() {
        User customer = User.builder()
                .id(1L)
                .firstName("Caleb")
                .lastName("Smith")
                .email("caleb@example.com")
                .build();
        Product product = product(10L, 5, true);
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepo.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));
        when(productRepo.save(product)).thenReturn(product);
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(20L);
            return order;
        });

        OrderResponse response = orderService.createOrder(1L, orderRequest(10L, 2));

        assertEquals(20L, response.getId());
        assertEquals(new BigDecimal("50.00"), response.getTotalAmount());
        assertEquals(3, product.getStockQuantity());
        assertEquals("caleb@example.com", response.getCustomerEmail());
        verify(productRepo).save(product);
    }

    // Confirms archived products cannot enter a new order.
    @Test
    void createOrderRejectsArchivedProduct() {
        User customer = User.builder().id(1L).build();
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepo.findByIdAndActiveTrue(10L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(1L, orderRequest(10L, 1)));

        verify(orderRepo, never()).save(any(Order.class));
    }

    // Confirms checkout rejects quantities greater than current stock.
    @Test
    void createOrderRejectsInsufficientStock() {
        User customer = User.builder().id(1L).build();
        Product product = product(10L, 1, true);
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepo.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));

        assertThrows(
                BusinessRuleException.class,
                () -> orderService.createOrder(1L, orderRequest(10L, 2)));

        verify(productRepo, never()).save(any(Product.class));
        verify(orderRepo, never()).save(any(Order.class));
    }

    // Confirms a customer cannot read an order belonging to another customer.
    @Test
    void getOrderForCustomerRejectsForeignOrder() {
        when(orderRepo.findByIdAndCustomerId(20L, 1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderForCustomer(1L, 20L));
    }

    // Confirms the admin service view maps complete order data without customer ownership filtering.
    @Test
    void getOrderForAdminReturnsAnyExistingOrder() {
        Order order = Order.builder()
                .id(20L)
                .customer_name("Caleb Smith")
                .customer_email("caleb@example.com")
                .status("CONFIRMED")
                .orderStatus(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PENDING)
                .fulfillmentStatus(FulfillmentStatus.PENDING)
                .total_amount(new BigDecimal("50.00"))
                .orderItems(new java.util.ArrayList<>())
                .build();
        when(orderRepo.findById(20L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderForAdmin(20L);

        assertEquals(20L, response.getId());
        assertEquals("caleb@example.com", response.getCustomerEmail());
        assertEquals(OrderStatus.CONFIRMED, response.getOrderStatus());
    }

    // Confirms customers can cancel only an owned order before fulfillment starts.
    @Test
    void cancelOrderMovesConfirmedOrderToCancelled() {
        Product product = product(10L, 3, true);
        OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(2)
                .build();
        Order order = Order.builder()
                .id(20L)
                .orderStatus(OrderStatus.CONFIRMED)
                .fulfillmentStatus(FulfillmentStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .orderItems(new java.util.ArrayList<>(List.of(item)))
                .build();
        item.setOrder(order);
        when(orderRepo.findByIdAndCustomerId(20L, 1L)).thenReturn(Optional.of(order));
        when(productRepo.save(product)).thenReturn(product);
        when(orderRepo.save(order)).thenReturn(order);

        OrderResponse response = orderService.cancelOrder(1L, 20L);

        assertEquals(OrderStatus.CANCELLED, response.getOrderStatus());
        assertEquals(FulfillmentStatus.CANCELLED, response.getFulfillmentStatus());
        assertEquals(5, product.getStockQuantity());
        verify(productRepo).save(product);
        verify(auditLogService).record(1L, AuditAction.ORDER_CANCELLED,
                "ORDER", 20L, "Order cancelled and stock restored");
    }

    // Confirms a paid order cannot be cancelled before refund handling exists.
    @Test
    void cancelOrderRejectsPaidOrder() {
        Product product = product(10L, 3, true);
        OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(2)
                .build();
        Order order = Order.builder()
                .id(20L)
                .orderStatus(OrderStatus.CONFIRMED)
                .fulfillmentStatus(FulfillmentStatus.PENDING)
                .paymentStatus(PaymentStatus.PAID)
                .orderItems(new java.util.ArrayList<>(List.of(item)))
                .build();
        when(orderRepo.findByIdAndCustomerId(20L, 1L)).thenReturn(Optional.of(order));

        assertThrows(
                BusinessRuleException.class,
                () -> orderService.cancelOrder(1L, 20L));

        verify(productRepo, never()).save(any(Product.class));
        verify(orderRepo, never()).save(order);
    }

    // Confirms cancellation is rejected after fulfillment has progressed.
    @Test
    void cancelOrderRejectsShippedOrder() {
        Order order = Order.builder()
                .id(20L)
                .orderStatus(OrderStatus.CONFIRMED)
                .fulfillmentStatus(FulfillmentStatus.SHIPPED)
                .build();
        when(orderRepo.findByIdAndCustomerId(20L, 1L)).thenReturn(Optional.of(order));

        assertThrows(
                BusinessRuleException.class,
                () -> orderService.cancelOrder(1L, 20L));
    }

    // Confirms a seller can advance fulfillment only for an owned order line.
    @Test
    void updateFulfillmentStatusAdvancesOwnedOrder() {
        Order order = Order.builder()
                .id(20L)
                .orderStatus(OrderStatus.CONFIRMED)
                .fulfillmentStatus(FulfillmentStatus.PENDING)
                .orderItems(new java.util.ArrayList<>())
                .build();
        when(orderItemRepo.existsByOrderIdAndProductSellerProfileUserId(20L, 30L))
                .thenReturn(true);
        when(orderRepo.findById(20L)).thenReturn(Optional.of(order));
        when(orderRepo.save(order)).thenReturn(order);
        when(orderItemRepo.findAllByProductSellerProfileUserId(30L))
                .thenReturn(List.of());

        assertEquals(
                FulfillmentStatus.PROCESSING,
                orderService.updateFulfillmentStatus(
                        30L,
                        20L,
                        FulfillmentStatusUpdateRequest.builder()
                                .status(FulfillmentStatus.PROCESSING)
                                .build())
                        .getFulfillmentStatus());
    }

    // Confirms invalid payment transitions are rejected before persistence.
    @Test
    void updatePaymentStatusRejectsRefundBeforePayment() {
        Order order = Order.builder()
                .id(20L)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        when(orderRepo.findById(20L)).thenReturn(Optional.of(order));

        assertThrows(
                BusinessRuleException.class,
                () -> orderService.updatePaymentStatus(20L, PaymentStatus.REFUNDED));
    }

    // Confirms checkout revalidates active products and stock, creates the order, then clears the cart.
    @Test
    void checkoutCartCreatesOrderAndClearsCart() {
        User customer = User.builder()
                .id(1L)
                .firstName("Caleb")
                .lastName("Smith")
                .email("caleb@example.com")
                .build();
        Product product = product(10L, 5, true);
        Cart cart = Cart.builder().id(5L).customer(customer).build();
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();
        cart.setItems(new java.util.ArrayList<>(List.of(cartItem)));

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepo.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(productRepo.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));
        when(productRepo.save(product)).thenReturn(product);
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(20L);
            return order;
        });
        when(cartRepo.save(cart)).thenReturn(cart);

        OrderResponse response = orderService.checkoutCart(1L);

        assertEquals(20L, response.getId());
        assertEquals(new BigDecimal("50.00"), response.getTotalAmount());
        assertEquals(3, product.getStockQuantity());
        assertEquals(0, cart.getItems().size());
        verify(cartRepo).save(cart);
    }

    // Confirms an empty cart cannot create a zero-line order.
    @Test
    void checkoutCartRejectsEmptyCart() {
        Cart cart = Cart.builder().id(5L).items(new java.util.ArrayList<>()).build();
        when(userRepo.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(cartRepo.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        assertThrows(
                BusinessRuleException.class,
                () -> orderService.checkoutCart(1L));

        verify(orderRepo, never()).save(any(Order.class));
    }

    // Confirms a stale cart line cannot purchase an archived product.
    @Test
    void checkoutCartRejectsArchivedProduct() {
        Product product = product(10L, 5, true);
        Cart cart = Cart.builder().id(5L).items(new java.util.ArrayList<>()).build();
        cart.getItems().add(CartItem.builder().cart(cart).product(product).quantity(1).build());
        when(userRepo.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(cartRepo.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(productRepo.findByIdAndActiveTrue(10L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.checkoutCart(1L));

        verify(orderRepo, never()).save(any(Order.class));
    }

    // Confirms sellers receive only their own product lines grouped by order.
    @Test
    void getOrdersForSellerReturnsSellerOnlyLinesAndSubtotal() {
        Product product = product(10L, 5, true);
        Order order = Order.builder()
                .id(20L)
                .status("CONFIRMED")
                .orderItems(new java.util.ArrayList<>())
                .build();
        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .priceAtPurchase(new BigDecimal("25.00"))
                .build();
        when(orderItemRepo.findAllByProductSellerProfileUserId(30L))
                .thenReturn(List.of(item));

        var responses = orderService.getOrdersForSeller(30L);

        assertEquals(1, responses.size());
        assertEquals(20L, responses.get(0).getOrderId());
        assertEquals(new BigDecimal("50.00"), responses.get(0).getSellerSubtotal());
        assertEquals(1, responses.get(0).getItems().size());
    }

    private OrderRequest orderRequest(Long productId, int quantity) {
        return OrderRequest.builder()
                .items(List.of(OrderItemRequest.builder()
                        .productId(productId)
                        .quantity(quantity)
                        .build()))
                .build();
    }

    private Product product(Long id, int stock, boolean active) {
        return Product.builder()
                .id(id)
                .name("Handmade Bag")
                .price(new BigDecimal("25.00"))
                .category("Accessories")
                .stockQuantity(stock)
                .active(active)
                .sellerProfile(SellerProfile.builder()
                        .id(30L)
                        .status(SellerApplicationStatus.APPROVED)
                        .build())
                .build();
    }
}
