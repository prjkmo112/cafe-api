package io.github.prjkmo112.cafeapi.domain.menu.dto;

import io.github.prjkmo112.cafeapi.domain.menu.entity.MenuStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MenuListRequestDto {

    private String keyword;
    private Long priceStart;
    private Long priceEnd;
    private MenuStatus status;

    private LocalDateTime createdAtStart;
    private LocalDateTime createdAtEnd;

}
