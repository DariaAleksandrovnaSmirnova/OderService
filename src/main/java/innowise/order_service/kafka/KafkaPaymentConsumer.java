package innowise.order_service.kafka;//package innowise.payment_service.kafka;

import innowise.common.event.PaymentEvent;
import innowise.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentConsumer {
    private final OrderService orderService;

    @KafkaListener(topics = "${kafka.topic.payment.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenCreatePayment(PaymentEvent event) {
        log.info("Received payment event for order: {}", event.orderId());
        orderService.changeStatus(event);
    }
}
