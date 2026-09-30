package io.github.prjkmo112.cafeapi.domain.order.consumer;

import io.github.prjkmo112.cafeapi.common.config.KafkaConsumerConfig;
import io.github.prjkmo112.cafeapi.common.dto.event.OrderPaidEvent;
import io.github.prjkmo112.cafeapi.common.dto.topic.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockDataOrderPaidConsumer {

    @KafkaListener(
            topics = KafkaTopics.ORDER_PAID_EVENT,
            groupId = KafkaConsumerConfig.ORDER_PAID_GROUP_ID,
            containerFactory = "orderPaidKafkaListenerContainerFactory"
    )
    public void consume(OrderPaidEvent event) {
        log.info(
                """
                
                |-----------------------------|
                | Consumed OrderPaidEvent
                |
                |   userId: {}
                |   menuId: {}
                |   paidAmount: {}
                |   orderId: {}
                |-----------------------------|
                
                """,
                event.userId(),
                event.menuId(),
                event.paidAmount(),
                event.orderId()
        );

        // 데이터 처리 로직...
        // ...
        // ...
    }

}
