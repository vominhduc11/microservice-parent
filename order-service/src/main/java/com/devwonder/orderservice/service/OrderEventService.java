package com.devwonder.orderservice.service;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.common.event.OrderNotificationEvent;
import com.devwonder.orderservice.client.UserServiceClient;
import com.devwonder.orderservice.constant.KafkaTopics;
import com.devwonder.orderservice.dto.DealerInfo;
import com.devwonder.orderservice.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserServiceClient userServiceClient;

    public void publishOrderNotificationEvent(Order order, BigDecimal totalAmount) {
        try {
            // Get dealer information from user-service
            DealerInfo dealerInfo = getDealerInfo(order.getIdDealer());

            OrderNotificationEvent event = OrderNotificationEvent.builder()
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .dealerId(order.getIdDealer())
                    .dealerName(dealerInfo != null ? dealerInfo.getCompanyName() : "Unknown")
                    .dealerEmail(dealerInfo != null ? dealerInfo.getEmail() : "")
                    .dealerPhone(dealerInfo != null ? dealerInfo.getPhone() : "")
                    .dealerCity(dealerInfo != null ? dealerInfo.getCity() : "")
                    .totalAmount(totalAmount)
                    .paymentStatus(order.getPaymentStatus().toString())
                    .orderTime(order.getCreateAt())
                    .build();

            kafkaTemplate.send(KafkaTopics.ORDER_NOTIFICATIONS, order.getId().toString(), event);
            log.info("Published order notification event for orderId: {} from dealer: {}",
                order.getId(), dealerInfo != null ? dealerInfo.getCompanyName() : "Unknown");
        } catch (Exception e) {
            log.error("Error publishing order notification event for orderId: {}", order.getId(), e);
        }
    }

    private DealerInfo getDealerInfo(Long dealerId) {
        try {
            BaseResponse<DealerInfo> response = userServiceClient.getDealerInfo(dealerId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch dealer info for dealerId: {}, using fallback values", dealerId, e);
        }
        return null;
    }
}