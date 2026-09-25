package io.github.prjkmo112.cafeapi.domain.point.controller;

import io.github.prjkmo112.cafeapi.common.dto.ApiResponse;
import io.github.prjkmo112.cafeapi.domain.point.dto.PointChargeRequestDto;
import io.github.prjkmo112.cafeapi.domain.point.dto.PointDto;
import io.github.prjkmo112.cafeapi.domain.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/charge")
    public ApiResponse<PointDto> charge(@Valid @RequestBody PointChargeRequestDto pointChargeRequestDto) {
        return ApiResponse.ok(pointService.charge(pointChargeRequestDto));
    }

}
