package innowise.order_service.kafka;

import innowise.common.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderProducer {
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final NewTopic topic;

    public void sendCreateOrder(OrderEvent event) {
        log.info("Sending event {} to Kafka with topic {}", event, topic.name());
        kafkaTemplate.send(topic.name(), event);
    }
}
