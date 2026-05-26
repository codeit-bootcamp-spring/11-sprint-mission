package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserLoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.auth.PasswordMismatchException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BasicAuthServiceTest {

  @InjectMocks
  private BasicAuthService authService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Test
  @DisplayName("로그인 성공")
  void login_success() {
    UserLoginRequest request = new UserLoginRequest("testUser", "password123");
    User user = new User("testUser", "test@email.com", "password123");
    UserDto userDto = new UserDto(UUID.randomUUID(), "testUser", "test@email.com", null, true);

    given(userRepository.findByUsername(request.username())).willReturn(Optional.of(user));
    given(userMapper.toDto(user)).willReturn(userDto);

    UserDto result = authService.login(request);

    assertThat(result.username()).isEqualTo("testUser");

    then(userMapper).should().toDto(user);
  }

  @Test
  @DisplayName("로그인 실패 - 존재하지 않는 유저")
  void login_fail_userNotFound() {
    UserLoginRequest request = new UserLoginRequest("unknownUser", "password123");

    given(userRepository.findByUsername(request.username())).willReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(request)).isInstanceOf(UserNotFoundException.class);

    then(userMapper).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void login_fail_wrongPassword() {
    UserLoginRequest request = new UserLoginRequest("testUser", "wrongPassword");
    User user = new User("testUser", "test@email.com", "password123");

    given(userRepository.findByUsername(request.username())).willReturn(Optional.of(user));

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(PasswordMismatchException.class);

    then(userMapper).shouldHaveNoInteractions();
  }
}