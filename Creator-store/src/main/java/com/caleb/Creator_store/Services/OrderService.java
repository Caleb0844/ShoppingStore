package com.caleb.Creator_store.Services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.caleb.Creator_store.Entities.Order;
import com.caleb.Creator_store.Entities.OrderItem;
import com.caleb.Creator_store.Entities.Product;
import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.Entities.Cart;
import com.caleb.Creator_store.Entities.CartItem;
import com.caleb.Creator_store.Repositories.CartRepo;
import com.caleb.Creator_store.Repositories.OrderItemRepo;
import com.caleb.Creator_store.Repositories.OrderRepo;
import com.caleb.Creator_store.Repositories.ProductRepo;
import com.caleb.Creator_store.Repositories.UserRepo;
import com.caleb.Creator_store.Enums.FulfillmentStatus;
import com.caleb.Creator_store.Enums.OrderStatus;
import com.caleb.Creator_store.Enums.PaymentStatus;
import com.caleb.Creator_store.Enums.AuditAction;
import com.caleb.Creator_store.dto.OrderItemRequest;
import com.caleb.Creator_store.dto.OrderItemResponse;
import com.caleb.Creator_store.dto.OrderRequest;
import com.caleb.Creator_store.dto.OrderResponse;
import com.caleb.Creator_store.dto.FulfillmentStatusUpdateRequest;
import com.caleb.Creator_store.exceptions.BusinessRuleException;
import com.caleb.Creator_store.exceptions.ResourceNotFoundException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// Coordinates authenticated customer checkout and customer-owned order history.
// Reuse this service boundary for future payment and fulfillment workflows.
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final CartRepo cartRepo;
    private final OrderItemRepo orderItemRepo;
    private final AuditLogService auditLogService;

    // Converts the authenticated customer's current cart into one multi-seller order.
    // Product status, stock, and price are revalidated here because cart data can become stale.
    @Transactional
    public OrderResponse checkoutCart(Long userId) {
        User customer = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + userId));
        Cart cart = cartRepo.findByCustomerId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Cannot checkout an empty cart");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        Order order = Order.builder()
                .customer(customer)
                .customer_name(customer.getFirstName() + " " + customer.getLastName())
                .customer_email(customer.getEmail())
                .status("CONFIRMED")
                .orderStatus(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PENDING)
                .fulfillmentStatus(FulfillmentStatus.PENDING)
                .build();

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepo.findByIdAndActiveTrue(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active product not found with id: " + cartItem.getProduct().getId()));
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BusinessRuleException(
                        "Not enough quantity for product: " + product.getName());
            }

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalPrice = totalPrice.add(lineTotal);
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepo.save(product);

            orderItems.add(OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build());
        }

        order.setOrderItems(orderItems);
        order.setTotal_amount(totalPrice);
        OrderResponse response = toResponse(orderRepo.save(order));
        cart.getItems().clear();
        cartRepo.save(cart);
        return response;
    }

    // Creates an order for the authenticated user using active products and authoritative prices.
    @Transactional
    public OrderResponse createOrder(Long userId, OrderRequest orderRequest) {
        User customer = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + userId));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        Order order = Order.builder()
                .customer(customer)
                .customer_name(customer.getFirstName() + " " + customer.getLastName())
                .customer_email(customer.getEmail())
                .status("CONFIRMED")
                .orderStatus(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PENDING)
                .fulfillmentStatus(FulfillmentStatus.PENDING)
                .build();

        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            Product product = productRepo.findByIdAndActiveTrue(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active product not found with id: " + itemRequest.getProductId()));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new BusinessRuleException("Not enough quantity for this product");
            }

            BigDecimal priceOfItems = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalPrice = totalPrice.add(priceOfItems);

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepo.save(product);

            orderItems.add(OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build());
        }

        order.setOrderItems(orderItems);
        order.setTotal_amount(totalPrice);
        return toResponse(orderRepo.save(order));
    }

    // Returns only the authenticated customer's complete order history.
    public List<OrderResponse> getOrdersForCustomer(Long userId) {
        return orderRepo.findAllByCustomerId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    // Returns one order only when it belongs to the authenticated customer.
    public OrderResponse getOrderForCustomer(Long userId, Long orderId) {
        return orderRepo.findByIdAndCustomerId(orderId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found for the authenticated customer"));
    }

    // Returns complete order data for an administrator's moderation and support workflows.
    public List<OrderResponse> getAllOrdersForAdmin() {
        return orderRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // Returns one complete order for an administrator.
    public OrderResponse getOrderForAdmin(Long orderId) {
        return orderRepo.findById(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));
    }

    // Cancels a customer-owned order and restores reserved stock before fulfillment starts.
    // Paid orders require a refund workflow first so cancellation cannot leave payment state inconsistent.
    @Transactional
    public OrderResponse cancelOrder(Long customerId, Long orderId) {
        Order order = orderRepo.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found for the authenticated customer"));
        if ((order.getOrderStatus() != OrderStatus.PENDING
                && order.getOrderStatus() != OrderStatus.CONFIRMED)
                || order.getFulfillmentStatus() != FulfillmentStatus.PENDING) {
            throw new BusinessRuleException(
                    "Only pending or confirmed orders awaiting fulfillment can be cancelled");
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BusinessRuleException(
                    "Paid orders require a refund before they can be cancelled");
        }

        // Stock is restored in the same transaction as the status change, preventing partial cancellation.
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepo.save(product);
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setFulfillmentStatus(FulfillmentStatus.CANCELLED);
        Order savedOrder = orderRepo.save(order);
        auditLogService.record(customerId, AuditAction.ORDER_CANCELLED,
                "ORDER", orderId, "Order cancelled and stock restored");
        return toResponse(savedOrder);
    }

    // Advances fulfillment only when the seller owns a line in the order.
    @Transactional
    public com.caleb.Creator_store.dto.SellerOrderResponse updateFulfillmentStatus(
            Long sellerId, Long orderId, FulfillmentStatusUpdateRequest request) {
        if (!orderItemRepo.existsByOrderIdAndProductSellerProfileUserId(orderId, sellerId)) {
            throw new ResourceNotFoundException(
                    "Order not found for the authenticated seller");
        }
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        validateFulfillmentTransition(order.getFulfillmentStatus(), request.getStatus());
        order.setFulfillmentStatus(request.getStatus());
        Order savedOrder = orderRepo.save(order);
        auditLogService.record(sellerId, AuditAction.FULFILLMENT_STATUS_CHANGED,
                "ORDER", orderId, "Fulfillment changed to " + request.getStatus());
        List<OrderItem> sellerItems = orderItemRepo
                .findAllByProductSellerProfileUserId(sellerId).stream()
                .filter(item -> item.getOrder().getId().equals(orderId))
                .toList();
        return toSellerResponse(savedOrder, sellerItems);
    }

    // Applies trusted payment-provider results without allowing arbitrary payment state changes.
    // Keep this service method internal until M-Pesa callback authentication is implemented.
    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, PaymentStatus requestedStatus) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        validatePaymentTransition(order.getPaymentStatus(), requestedStatus);
        order.setPaymentStatus(requestedStatus);
        Order savedOrder = orderRepo.save(order);
        auditLogService.record(null, AuditAction.PAYMENT_STATUS_CHANGED,
                "ORDER", orderId, "Payment changed to " + requestedStatus);
        return toResponse(savedOrder);
    }

    // Returns grouped order lines for the authenticated seller without exposing customer data.
    public List<com.caleb.Creator_store.dto.SellerOrderResponse> getOrdersForSeller(Long userId) {
        return orderItemRepo.findAllByProductSellerProfileUserId(userId).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        OrderItem::getOrder,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()))
                .entrySet().stream()
                .map(entry -> toSellerResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    // Maps only this seller's lines and calculates the seller-specific subtotal.
    private com.caleb.Creator_store.dto.SellerOrderResponse toSellerResponse(
            Order order, List<OrderItem> sellerItems) {
        List<com.caleb.Creator_store.dto.SellerOrderItemResponse> items = sellerItems.stream()
                .map(item -> com.caleb.Creator_store.dto.SellerOrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .lineTotal(item.getPriceAtPurchase()
                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();
        BigDecimal subtotal = items.stream()
                .map(com.caleb.Creator_store.dto.SellerOrderItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return com.caleb.Creator_store.dto.SellerOrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .fulfillmentStatus(order.getFulfillmentStatus())
                .createdAt(order.getCreatedAt())
                .sellerSubtotal(subtotal)
                .items(items)
                .build();
    }

    private void validateFulfillmentTransition(
            FulfillmentStatus currentStatus, FulfillmentStatus requestedStatus) {
        boolean valid = (currentStatus == FulfillmentStatus.PENDING
                && requestedStatus == FulfillmentStatus.PROCESSING)
                || (currentStatus == FulfillmentStatus.PROCESSING
                && requestedStatus == FulfillmentStatus.SHIPPED)
                || (currentStatus == FulfillmentStatus.SHIPPED
                && requestedStatus == FulfillmentStatus.DELIVERED)
                || ((currentStatus == FulfillmentStatus.PENDING
                || currentStatus == FulfillmentStatus.PROCESSING)
                && requestedStatus == FulfillmentStatus.CANCELLED);
        if (!valid) {
            throw new BusinessRuleException(
                    "Fulfillment cannot change from " + currentStatus + " to " + requestedStatus);
        }
    }

    private void validatePaymentTransition(
            PaymentStatus currentStatus, PaymentStatus requestedStatus) {
        boolean valid = (currentStatus == PaymentStatus.PENDING
                && (requestedStatus == PaymentStatus.PAID
                || requestedStatus == PaymentStatus.FAILED))
                || (currentStatus == PaymentStatus.PAID
                && requestedStatus == PaymentStatus.REFUNDED);
        if (!valid) {
            throw new BusinessRuleException(
                    "Payment cannot change from " + currentStatus + " to " + requestedStatus);
        }
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .sellerProfileId(item.getProduct().getSellerProfile().getId())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomer_name())
                .customerEmail(order.getCustomer_email())
                .status(order.getStatus())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .fulfillmentStatus(order.getFulfillmentStatus())
                .totalAmount(order.getTotal_amount())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
