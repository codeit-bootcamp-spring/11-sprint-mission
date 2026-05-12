package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.auth.InvalidPasswordException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicAuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BasicAuthService authService;

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("taehk23", "password");
        User user = new User("taehk23", "taehk23@test.com", "password", null);
        UserDto expected = new UserDto(UUID.randomUUID(), "taehk23", "taehk23@test.com", null, true);

        given(userRepo.findByUsername("taehk23")).willReturn(Optional.of(user));
        given(userMapper.toDto(user)).willReturn(expected);

        UserDto result = authService.login(request);

        assertThat(result).isEqualTo(expected);

        then(userRepo).should().findByUsername("taehk23");
        then(userMapper).should().toDto(user);
    }

    @Test
    void login_fail_whenUserNotFound() {
        LoginRequest request = new LoginRequest("taehk23", "password");

        given(userRepo.findByUsername("taehk23")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepo).should().findByUsername("taehk23");
        then(userMapper).should(never()).toDto(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void login_fail_whenPasswordInvalid() {
        LoginRequest request = new LoginRequest("taehk23", "wrong-password");
        User user = new User("taehk23", "taehk23@test.com", "password", null);

        given(userRepo.findByUsername("taehk23")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidPasswordException.class);

        then(userRepo).should().findByUsername("taehk23");
        then(userMapper).should(never()).toDto(org.mockito.ArgumentMatchers.any());
    }
}
