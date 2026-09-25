package io.github.prjkmo112.cafeapi.domain.user.service;

import io.github.prjkmo112.cafeapi.domain.point.entity.UserPoint;
import io.github.prjkmo112.cafeapi.domain.point.repository.UserPointRepository;
import io.github.prjkmo112.cafeapi.domain.user.dto.UserDto;
import io.github.prjkmo112.cafeapi.domain.user.dto.UserRegisterRequestDto;
import io.github.prjkmo112.cafeapi.domain.user.entity.User;
import io.github.prjkmo112.cafeapi.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserPointRepository userPointRepository;

    @Transactional
    public UserDto register(UserRegisterRequestDto userRegisterRequestDto) {
        User user = userRegisterRequestDto.toEntity();
        userRepository.save(user);

        UserPoint userPoint = UserPoint.builder()
                .user(user)
                .build();
        userPointRepository.save(userPoint);

        return UserDto.from(user);
    }

}
