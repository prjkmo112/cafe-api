package io.github.prjkmo112.cafeapi.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,            // 현재 페이지 (0부터)
        int size,            // 페이지 크기
        long totalElements,  // 전체 개수
        int totalPages,      // 전체 페이지 수
        boolean hasNext
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
