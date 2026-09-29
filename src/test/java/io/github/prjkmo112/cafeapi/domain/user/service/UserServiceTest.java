package io.github.prjkmo112.cafeapi.domain.user.service;

import io.github.prjkmo112.cafeapi.common.exception.BusinessException;
import io.github.prjkmo112.cafeapi.common.exception.ErrorCode;
import io.github.prjkmo112.cafeapi.domain.point.entity.UserPoint;
import io.github.prjkmo112.cafeapi.domain.point.repository.UserPointRepository;
import io.github.prjkmo112.cafeapi.domain.user.dto.UserDto;
import io.github.prjkmo112.cafeapi.domain.user.dto.UserRegisterRequestDto;
import io.github.prjkmo112.cafeapi.domain.user.entity.User;
import io.github.prjkmo112.cafeapi.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final UserRegisterRequestDto REQUEST =
            new UserRegisterRequestDto("홍길동", "hong@example.com", "password1234");

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPointRepository userPointRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("가입하면 사용자가 저장되고 잔액 0원의 user_point 도 함께 생성된다")
    void register_success() {
        // given
        given(userRepository.existsByEmail("hong@example.com")).willReturn(false);

        // when
        UserDto result = userService.register(REQUEST);

        // then
        assertThat(result.name()).isEqualTo("홍길동");
        assertThat(result.email()).isEqualTo("hong@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("hong@example.com");

        ArgumentCaptor<UserPoint> pointCaptor = ArgumentCaptor.forClass(UserPoint.class);
        verify(userPointRepository).save(pointCaptor.capture());
        assertThat(pointCaptor.getValue().getBalance()).isZero();
        assertThat(pointCaptor.getValue().getUser()).isSameAs(userCaptor.getValue());
    }

    @Test
    @DisplayName("이미 존재하는 이메일이면 DUPLICATE_EMAIL 이고 아무것도 저장하지 않는다")
    void register_duplicateEmail() {
        // given
        given(userRepository.existsByEmail("hong@example.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.register(REQUEST))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        // then
        verify(userRepository, never()).save(any());
        verify(userPointRepository, never()).save(any());
    }

    @Test
    @DisplayName("동시 가입으로 유니크 제약에 걸리면 DUPLICATE_EMAIL 이고 user_point 를 만들지 않는다")
    void register_concurrentDuplicateEmail() {
        // given
        given(userRepository.existsByEmail("hong@example.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willThrow(new DataIntegrityViolationException("users.email"));

        // when & then
        assertThatThrownBy(() -> userService.register(REQUEST))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        // then
        verify(userPointRepository, never()).save(any());
    }

}
