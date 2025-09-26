package com.devwonder.orderservice.service;

import com.devwonder.orderservice.dto.CreateOrderRequest;
import com.devwonder.orderservice.dto.DealerOrderStats;
import com.devwonder.orderservice.dto.DealerResponse;
import com.devwonder.orderservice.dto.OrderResponse;
import com.devwonder.orderservice.dto.OrderItemResponse;
import com.devwonder.orderservice.entity.Order;
import com.devwonder.orderservice.entity.OrderItem;
import com.devwonder.common.enums.OrderItemStatus;
import com.devwonder.orderservice.enums.PaymentStatus;
import com.devwonder.orderservice.mapper.OrderMapper;
import com.devwonder.orderservice.repository.OrderRepository;
import com.devwonder.orderservice.repository.OrderItemRepository;
import com.devwonder.orderservice.client.UserServiceClient;
import com.devwonder.common.dto.BaseResponse;
import com.devwonder.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final UserServiceClient userServiceClient;

    @Value("${auth.api.key:INTER_SERVICE_KEY}")
    private String authApiKey;

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

        List<Order> orders = orderRepository.findByIsDeletedFalseOrderByCreatedAtDesc();

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

        List<Order> orders = orderRepository.findByIdDealerAndIsDeletedFalseOrderByCreatedAtDesc(dealerId);

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

        List<Order> orders = orderRepository.findByIsDeletedTrueOrderByCreatedAtDesc();

        return orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                    return buildOrderResponse(order, orderItems);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DealerOrderStats> getDealerOrderStats() {
        log.info("Retrieving dealer order statistics");

        // Get all unique dealer IDs from orders
        List<Long> dealerIds = orderRepository.findDistinctDealerIds();

        return dealerIds.stream()
                .map(this::buildDealerOrderStats)
                .collect(Collectors.toList());
    }


    private DealerOrderStats buildDealerOrderStats(Long dealerId) {
        // Get all orders for this dealer
        List<Order> dealerOrders = orderRepository.findByIdDealerAndIsDeletedFalseOrderByCreatedAtDesc(dealerId);

        if (dealerOrders.isEmpty()) {
            return null;
        }

        // Calculate statistics
        long totalOrders = dealerOrders.size();
        long paidOrders = dealerOrders.stream().mapToLong(order ->
            order.getPaymentStatus() == PaymentStatus.PAID ? 1 : 0).sum();
        long unpaidOrders = totalOrders - paidOrders;

        // Calculate total revenue from paid orders
        BigDecimal totalRevenue = dealerOrders.stream()
                .filter(order -> order.getPaymentStatus() == PaymentStatus.PAID)
                .map(order -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                    return orderItems.stream()
                            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime firstOrderDate = dealerOrders.get(dealerOrders.size() - 1).getCreatedAt();
        LocalDateTime lastOrderDate = dealerOrders.get(0).getCreatedAt();

        // Get dealer information from user service
        DealerResponse dealerInfo = getDealerInfo(dealerId);

        return DealerOrderStats.builder()
                .dealerId(dealerId)
                .companyName(dealerInfo != null ? dealerInfo.getCompanyName() : "Unknown")
                .email(dealerInfo != null ? dealerInfo.getEmail() : "")
                .phone(dealerInfo != null ? dealerInfo.getPhone() : "")
                .city(dealerInfo != null ? dealerInfo.getCity() : "")
                .totalOrders(totalOrders)
                .paidOrders(paidOrders)
                .unpaidOrders(unpaidOrders)
                .totalRevenue(totalRevenue)
                .firstOrderDate(firstOrderDate)
                .lastOrderDate(lastOrderDate)
                .build();
    }

    private DealerResponse getDealerInfo(Long dealerId) {
        try {
            BaseResponse<DealerResponse> response = userServiceClient.getDealerInfo(dealerId, authApiKey);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch dealer info for dealerId: {}, using fallback values", dealerId, e);
        }
        return null;
    }

    private OrderResponse buildOrderResponse(Order order, List<OrderItem> orderItems) {
        // Set order items for mapping
        order.setOrderItems(orderItems);
        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public void updateOrderItemStatus(Long orderItemId, OrderItemStatus status) {
        log.info("Updating order item {} status to {}", orderItemId, status);

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with ID: " + orderItemId));

        orderItem.setStatus(status);
        orderItemRepository.save(orderItem);

        log.info("Successfully updated order item {} status to {}", orderItemId, status);
    }

    public OrderItemResponse getOrderItem(Long orderItemId) {
        log.info("Getting order item details for ID: {}", orderItemId);

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with ID: " + orderItemId));

        OrderItemResponse response = OrderItemResponse.builder()
                .id(orderItem.getId())
                .unitPrice(orderItem.getUnitPrice())
                .quantity(orderItem.getQuantity())
                .idProduct(orderItem.getIdProduct())
                .idOrder(orderItem.getIdOrder())
                .status(orderItem.getStatus())
                .build();

        log.info("Successfully retrieved order item details for ID: {}", orderItemId);
        return response;
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