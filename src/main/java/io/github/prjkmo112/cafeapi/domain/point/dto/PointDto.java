package io.github.prjkmo112.cafeapi.domain.point.dto;

import io.github.prjkmo112.cafeapi.domain.point.entity.PointHistory;
import io.github.prjkmo112.cafeapi.domain.point.entity.UserPoint;

public record PointDto(
        Long userId,
        Long point
) {

    public static PointDto from(PointHistory pointHistory) {
        return new PointDto(pointHistory.getUser().getId(), pointHistory.getBalanceAfter());
    }

    public static PointDto from(UserPoint userPoint) {
        return new PointDto(userPoint.getUser().getId(), userPoint.getBalance());
    }

}
