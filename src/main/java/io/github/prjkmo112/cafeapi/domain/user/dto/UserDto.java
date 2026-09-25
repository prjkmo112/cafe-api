package io.github.prjkmo112.cafeapi.domain.user.dto;

import io.github.prjkmo112.cafeapi.domain.user.entity.User;

public record UserDto(
        String email,
        String name
) {

    public static UserDto from(User user) {
        return new UserDto(user.getEmail(), user.getName());
    }

}
