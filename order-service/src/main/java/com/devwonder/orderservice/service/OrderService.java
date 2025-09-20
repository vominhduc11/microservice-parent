package com.devwonder.orderservice.service;

import com.devwonder.orderservice.dto.CreateOrderRequest;
import com.devwonder.orderservice.dto.OrderResponse;
import com.devwonder.orderservice.entity.Order;
import com.devwonder.orderservice.entity.OrderItem;
import com.devwonder.orderservice.enums.PaymentStatus;
import com.devwonder.orderservice.repository.OrderRepository;
import com.devwonder.orderservice.repository.OrderItemRepository;
import com.devwonder.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for dealer {}", request.getIdDealer());

        // Create Order entity
        Order order = Order.builder()
                .idDealer(request.getIdDealer())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Created order with ID: {}", savedOrder.getId());

        // Create OrderItems
        List<OrderItem> orderItems = request.getOrderItems().stream()
                .map(itemRequest -> OrderItem.builder()
                        .idProduct(itemRequest.getIdProduct())
                        .unitPrice(itemRequest.getUnitPrice())
                        .quantity(itemRequest.getQuantity())
                        .order(savedOrder)
                        .build())
                .collect(Collectors.toList());

        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);
        log.info("Created {} order items for order {}", savedOrderItems.size(), savedOrder.getId());

        return buildOrderResponse(savedOrder, savedOrderItems);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("Retrieving all orders");

        List<Order> orders = orderRepository.findAllByOrderByCreateAtDesc();

        return orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                    return buildOrderResponse(order, orderItems);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getDealerOrders(Long dealerId) {
        log.info("Retrieving orders for dealer {}", dealerId);

        List<Order> orders = orderRepository.findByIdDealerOrderByCreateAtDesc(dealerId);

        return orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                    return buildOrderResponse(order, orderItems);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        log.info("Retrieving order {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        return buildOrderResponse(order, orderItems);
    }

    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        log.info("Updating payment status for order {} to {}", orderId, paymentStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        order.setPaymentStatus(paymentStatus);
        Order updatedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        log.info("Successfully updated payment status for order {} to {}", orderId, paymentStatus);
        return buildOrderResponse(updatedOrder, orderItems);
    }

    private OrderResponse buildOrderResponse(Order order, List<OrderItem> orderItems) {
        List<OrderResponse.OrderItemResponse> itemResponses = orderItems.stream()
                .map(item -> {
                    BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return OrderResponse.OrderItemResponse.builder()
                            .id(item.getId())
                            .idProduct(item.getIdProduct())
                            .unitPrice(item.getUnitPrice())
                            .quantity(item.getQuantity())
                            .subtotal(subtotal)
                            .build();
                })
                .collect(Collectors.toList());

        // Calculate total price
        BigDecimal totalPrice = itemResponses.stream()
                .map(OrderResponse.OrderItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderResponse.builder()
                .id(order.getId())
                .idDealer(order.getIdDealer())
                .createAt(order.getCreateAt())
                .paymentStatus(order.getPaymentStatus())
                .orderItems(itemResponses)
                .totalPrice(totalPrice)
                .build();
    }
}