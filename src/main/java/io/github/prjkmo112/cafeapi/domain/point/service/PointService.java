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
import org.springframework.transaction.annotation.Propagation;
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
        // 멱등키 선조회. 동시 요청은 아래 유니크 제약 예외로 걸러짐
        Optional<PointHistory> pointHistory = pointHistoryRepository.findByUserIdAndIdempotencyKey(dto.userId(), dto.idempotencyKey());
        if (pointHistory.isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_POINT_CHARGE_REQUEST);
        }

        // 비관적 락으로 잔액 갱신 직렬화
        UserPoint userPoint = userPointRepository.findByUserIdForUpdate(dto.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        userPoint.charge(dto.point());

        PointHistory newHistory = PointHistory.builder()
                .user(userPoint.getUser())
                .type(PointHistoryType.CHARGE)
                .amount(dto.point())
                .balanceAfter(userPoint.getBalance())
                .idempotencyKey(dto.idempotencyKey())
                .build();

        try {
            pointHistoryRepository.save(newHistory);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_POINT_CHARGE_REQUEST);
        }

        return PointDto.from(userPoint);
    }

    // 주문 트랜잭션에 참여해야 하므로 단독 호출 불가
    @Transactional(propagation = Propagation.MANDATORY)
    public PointDto use(Long userId, Long amount) {
        UserPoint userPoint = userPointRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        userPoint.use(amount);

        PointHistory newHistory = PointHistory.builder()
                .user(userPoint.getUser())
                .type(PointHistoryType.USE)
                .amount(amount)
                .balanceAfter(userPoint.getBalance())
                .build();

        pointHistoryRepository.save(newHistory);

        return PointDto.from(userPoint);
    }

}
