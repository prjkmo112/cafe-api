package io.github.prjkmo112.cafeapi.domain.order.service;

import io.github.prjkmo112.cafeapi.common.dto.event.OrderPaidEvent;
import io.github.prjkmo112.cafeapi.common.exception.BusinessException;
import io.github.prjkmo112.cafeapi.common.exception.ErrorCode;
import io.github.prjkmo112.cafeapi.domain.menu.entity.Menu;
import io.github.prjkmo112.cafeapi.domain.order.dto.CreateOrderRequestDto;
import io.github.prjkmo112.cafeapi.domain.order.dto.OrderDto;
import io.github.prjkmo112.cafeapi.domain.order.entity.Order;
import io.github.prjkmo112.cafeapi.domain.order.repository.OrderRepository;
import io.github.prjkmo112.cafeapi.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Optional<OrderDto> findByIdempotencyKey(Long userId, String idempotencyKey) {
        return orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey).map(OrderDto::from);
    }

    // OrderFacade 의 결제 트랜잭션 안에서만 호출
    @Transactional(propagation = Propagation.MANDATORY)
    public OrderDto createOrder(CreateOrderRequestDto createOrderRequestDto, Menu menu) {
        Order order = Order.builder()
                .user(userRepository.getReferenceById(createOrderRequestDto.userId()))
                .menu(menu)
                .amount(menu.getPrice())
                .idempotencyKey(createOrderRequestDto.idempotencyKey())
                .build();

        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            // 동시 중복 요청: 전체 트랜잭션(포인트 차감 포함)이 롤백됨
            throw new BusinessException(ErrorCode.DUPLICATE_ORDER_REQUEST);
        }

        // kafka event 전송
        // ApplicationEventPublisher 에 의해 이벤트 그냥 뿌리고 넘어감
        // 그럼 매칭되는 @TransactionalEventListener 가 있는 OrderProducer.send() 가 호출됨
        eventPublisher.publishEvent(OrderPaidEvent.from(order));

        return OrderDto.from(order);
    }

}
