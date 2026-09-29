package io.github.prjkmo112.cafeapi.domain.user.service;

import io.github.prjkmo112.cafeapi.domain.point.entity.UserPoint;
import io.github.prjkmo112.cafeapi.domain.point.repository.UserPointRepository;
import io.github.prjkmo112.cafeapi.domain.user.dto.UserDto;
import io.github.prjkmo112.cafeapi.domain.user.dto.UserRegisterRequestDto;
import io.github.prjkmo112.cafeapi.domain.user.entity.User;
import io.github.prjkmo112.cafeapi.domain.user.repository.UserRepository;
import io.github.prjkmo112.cafeapi.common.exception.BusinessException;
import io.github.prjkmo112.cafeapi.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
        if (userRepository.existsByEmail(userRegisterRequestDto.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = userRegisterRequestDto.toEntity();
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // 선조회와 INSERT 사이에 동시 가입이 먼저 들어온 경우 (users.email 유니크 제약)
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        UserPoint userPoint = UserPoint.builder()
                .user(user)
                .build();
        userPointRepository.save(userPoint);

        return UserDto.from(user);
    }

}
