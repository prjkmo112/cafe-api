package io.github.prjkmo112.cafeapi.domain.order.controller;

import io.github.prjkmo112.cafeapi.common.dto.ApiResponse;
import io.github.prjkmo112.cafeapi.domain.order.dto.CreateOrderRequestDto;
import io.github.prjkmo112.cafeapi.domain.order.dto.OrderDto;
import io.github.prjkmo112.cafeapi.domain.order.facade.OrderFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;

    @PostMapping
    public ApiResponse<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequestDto createOrderRequestDto) {
        return ApiResponse.ok(orderFacade.createOrder(createOrderRequestDto));
    }

}
