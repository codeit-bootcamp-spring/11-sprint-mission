package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BasicAuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    BasicAuthService authService;

    @Test
    void login_success() {
        // given
        LoginRequest request = new LoginRequest("evan", "password123");
        User user = new User("evan", "evan@test.com", "password123");

        UserDto expectedDto = new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null,
                false
        );

        given(userRepository.findByUsernameAndPassword(request.username(), request.password()))
                .willReturn(Optional.of(user));
        given(userMapper.toDto(user)).willReturn(expectedDto);

        // when
        UserDto result = authService.login(request);

        // then
        assertThat(result).isEqualTo(expectedDto);
        then(userRepository).should().findByUsernameAndPassword(request.username(), request.password());
        then(userMapper).should().toDto(user);
    }

    @Test
    void login_fail_whenUsernameOrPasswordInvalid() {
        // given
        LoginRequest request = new LoginRequest("evan", "wrong");

        given(userRepository.findByUsernameAndPassword(request.username(), request.password()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DiscodeitException.class);

        then(userRepository).should().findByUsernameAndPassword(request.username(), request.password());
    }
}