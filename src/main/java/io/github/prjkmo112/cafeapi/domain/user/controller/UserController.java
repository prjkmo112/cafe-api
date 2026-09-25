package io.github.prjkmo112.cafeapi.domain.user.controller;

import io.github.prjkmo112.cafeapi.common.dto.ApiResponse;
import io.github.prjkmo112.cafeapi.domain.user.dto.UserDto;
import io.github.prjkmo112.cafeapi.domain.user.dto.UserRegisterRequestDto;
import io.github.prjkmo112.cafeapi.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserDto> register(@Valid @RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        return ApiResponse.ok(userService.register(userRegisterRequestDto));
    }

}
