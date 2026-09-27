package io.github.prjkmo112.cafeapi.domain.order.producer;

import io.github.prjkmo112.cafeapi.common.dto.event.OrderPaidEvent;
import io.github.prjkmo112.cafeapi.common.dto.topic.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderPaidEvent> orderPaidEventKafkaTemplate;

    public void send(OrderPaidEvent event) {
        orderPaidEventKafkaTemplate.send(KafkaTopics.ORDER_PAID_EVENT, event);
    }

}
