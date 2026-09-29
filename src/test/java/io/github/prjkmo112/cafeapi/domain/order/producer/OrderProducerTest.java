package io.github.prjkmo112.cafeapi.domain.order.producer;

import io.github.prjkmo112.cafeapi.common.dto.event.OrderPaidEvent;
import io.github.prjkmo112.cafeapi.common.dto.topic.KafkaTopics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderProducerTest {

    @Mock
    private KafkaTemplate<String, OrderPaidEvent> orderPaidEventKafkaTemplate;

    @InjectMocks
    private OrderProducer orderProducer;

    @Test
    @DisplayName("OrderPaidEvent 를 order-paid 토픽으로 전송한다")
    void send_publishesToOrderPaidTopic() {
        // given
        OrderPaidEvent event = OrderPaidEvent.builder()
                .userId(1L).menuId(10L).paidAmount(4000L).orderId("ORD-1").build();

        // when
        orderProducer.send(event);

        // then
        verify(orderPaidEventKafkaTemplate).send(KafkaTopics.ORDER_PAID_EVENT, event);
    }

}
