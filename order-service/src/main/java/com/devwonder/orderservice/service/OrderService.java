package com.devwonder.orderservice.service;

import com.devwonder.orderservice.dto.CreateOrderRequest;
import com.devwonder.orderservice.dto.OrderResponse;
import com.devwonder.orderservice.entity.Order;
import com.devwonder.orderservice.entity.OrderItem;
import com.devwonder.orderservice.enums.PaymentStatus;
import com.devwonder.orderservice.mapper.OrderMapper;
import com.devwonder.orderservice.repository.OrderRepository;
import com.devwonder.orderservice.repository.OrderItemRepository;
import com.devwonder.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventService orderEventService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for dealer {}", request.getIdDealer());

        // Generate unique order code
        String orderCode = generateOrderCode();

        // Create Order entity
        Order order = Order.builder()
                .idDealer(request.getIdDealer())
                .orderCode(orderCode)
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

        // Calculate total amount and publish notification event when order is created
        BigDecimal totalAmount = savedOrderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        orderEventService.publishOrderNotificationEvent(savedOrder, totalAmount);

        return buildOrderResponse(savedOrder, savedOrderItems);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("Retrieving all non-deleted orders");

        List<Order> orders = orderRepository.findByIsDeletedFalseOrderByCreateAtDesc();

        return orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                    return buildOrderResponse(order, orderItems);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getDealerOrders(Long dealerId) {
        log.info("Retrieving non-deleted orders for dealer {}", dealerId);

        List<Order> orders = orderRepository.findByIdDealerAndIsDeletedFalseOrderByCreateAtDesc(dealerId);

        return orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                    return buildOrderResponse(order, orderItems);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        log.info("Retrieving non-deleted order {}", orderId);

        Order order = orderRepository.findByIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        return buildOrderResponse(order, orderItems);
    }

    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        log.info("Updating payment status for order {} to {}", orderId, paymentStatus);

        Order order = orderRepository.findByIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        order.setPaymentStatus(paymentStatus);
        Order updatedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        log.info("Successfully updated payment status for order {} to {}", orderId, paymentStatus);
        return buildOrderResponse(updatedOrder, orderItems);
    }

    @Transactional
    public OrderResponse softDeleteOrder(Long orderId) {
        log.info("Soft deleting order {}", orderId);

        Order order = orderRepository.findByIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Check if order is paid before allowing soft delete
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalArgumentException("Cannot delete order with ID " + orderId + ". Only orders with PAID status can be deleted.");
        }

        order.setIsDeleted(true);
        Order deletedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        log.info("Successfully soft deleted order {}", orderId);
        return buildOrderResponse(deletedOrder, orderItems);
    }

    @Transactional
    public void hardDeleteOrder(Long orderId) {
        log.info("Hard deleting order {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Delete order items first due to foreign key constraint
        orderItemRepository.deleteByOrderId(orderId);

        // Then delete the order
        orderRepository.delete(order);

        log.info("Successfully hard deleted order {}", orderId);
    }

    @Transactional
    public OrderResponse restoreOrder(Long orderId) {
        log.info("Restoring order {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getIsDeleted()) {
            throw new IllegalArgumentException("Order with ID " + orderId + " is not deleted");
        }

        order.setIsDeleted(false);
        Order restoredOrder = orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        log.info("Successfully restored order {}", orderId);
        return buildOrderResponse(restoredOrder, orderItems);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getDeletedOrders() {
        log.info("Retrieving all deleted orders");

        List<Order> orders = orderRepository.findByIsDeletedTrueOrderByCreateAtDesc();

        return orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                    return buildOrderResponse(order, orderItems);
                })
                .collect(Collectors.toList());
    }

    private OrderResponse buildOrderResponse(Order order, List<OrderItem> orderItems) {
        // Set order items for mapping
        order.setOrderItems(orderItems);
        return orderMapper.toOrderResponse(order);
    }

    private String generateOrderCode() {
        // Format: ORD-YYYYMMDD-HHMMSS-XXXX (where XXXX is random)
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timePart = now.format(DateTimeFormatter.ofPattern("HHmmss"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        return String.format("ORD-%s-%s-%s", datePart, timePart, randomPart);
    }
}