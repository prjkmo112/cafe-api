package io.github.prjkmo112.cafeapi.domain.point.service;

import io.github.prjkmo112.cafeapi.common.exception.BusinessException;
import io.github.prjkmo112.cafeapi.common.exception.ErrorCode;
import io.github.prjkmo112.cafeapi.domain.point.dto.PointChargeRequestDto;
import io.github.prjkmo112.cafeapi.domain.point.dto.PointDto;
import io.github.prjkmo112.cafeapi.domain.point.entity.PointHistory;
import io.github.prjkmo112.cafeapi.domain.point.entity.PointHistoryType;
import io.github.prjkmo112.cafeapi.domain.point.entity.UserPoint;
import io.github.prjkmo112.cafeapi.domain.point.repository.PointHistoryRepository;
import io.github.prjkmo112.cafeapi.domain.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserPointRepository userPointRepository;

    @Transactional
    public PointDto charge(PointChargeRequestDto dto) {
        Optional<PointHistory> pointHistory = pointHistoryRepository.findByUserIdAndIdempotencyKey(dto.userId(), dto.idempotencyKey());
        if (pointHistory.isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_POINT_CHARGE_REQUEST);
        }

        UserPoint userPoint = userPointRepository.findByUserIdForUpdate(dto.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        userPoint.charge(dto.point());

        try {
            PointHistory newHistory = PointHistory.builder()
                    .user(userPoint.getUser())
                    .type(PointHistoryType.CHARGE)
                    .amount(dto.point())
                    .balanceAfter(userPoint.getBalance())
                    .idempotencyKey(dto.idempotencyKey())
                    .build();

            pointHistoryRepository.save(newHistory);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_POINT_CHARGE_REQUEST);
        }

        return PointDto.from(userPoint);
    }

}
