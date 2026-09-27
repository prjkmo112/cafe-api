package io.github.prjkmo112.cafeapi.domain.order.service;

import io.github.prjkmo112.cafeapi.common.dto.event.OrderPaidEvent;
import io.github.prjkmo112.cafeapi.domain.menu.entity.Menu;
import io.github.prjkmo112.cafeapi.domain.order.dto.CreateOrderRequestDto;
import io.github.prjkmo112.cafeapi.domain.order.dto.OrderDto;
import io.github.prjkmo112.cafeapi.domain.order.entity.Order;
import io.github.prjkmo112.cafeapi.domain.order.producer.OrderProducer;
import io.github.prjkmo112.cafeapi.domain.order.repository.OrderRepository;
import io.github.prjkmo112.cafeapi.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderProducer orderProducer;

    @Transactional(propagation = Propagation.MANDATORY)
    public OrderDto createOrder(CreateOrderRequestDto createOrderRequestDto, Menu menu) {
        Order order = Order.builder()
                .user(userRepository.getReferenceById(createOrderRequestDto.userId()))
                .menu(menu)
                .amount(menu.getPrice())
                .build();
        orderRepository.save(order);

        OrderPaidEvent orderPaidEvent = OrderPaidEvent.builder()
                .userId(order.getUser().getId())
                .menuId(order.getMenu().getId())
                .paidAmount(order.getAmount())
                .orderId(order.getOrderId())
                .build();
        orderProducer.send(orderPaidEvent);

        return OrderDto.from(order);
    }

}
