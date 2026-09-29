package io.github.prjkmo112.cafeapi.domain.order.producer;

import io.github.prjkmo112.cafeapi.common.dto.event.OrderPaidEvent;
import io.github.prjkmo112.cafeapi.common.dto.topic.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderPaidEvent> orderPaidEventKafkaTemplate;

    // 커밋 이후 별도 스레드에서 발행해 Kafka 지연/장애가 주문 응답에 영향을 주지 않게 함
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(OrderPaidEvent event) {
        orderPaidEventKafkaTemplate.send(KafkaTopics.ORDER_PAID_EVENT, event);
    }

}
