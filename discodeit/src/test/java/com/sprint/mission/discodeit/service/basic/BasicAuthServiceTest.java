package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.auth.InvalidCredentialsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicAuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper mapper;

  @InjectMocks
  private BasicAuthService authService;

  private UUID userId;
  private String username;
  private String email;
  private String password;
  private User user;
  private UserResponse response;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    username = "tester";
    email = "tester@example.io";
    password = "qwerty";
    user = new User(username, email, password, null);
    user.initStatus(new UserStatus(user));
    ReflectionTestUtils.setField(user, "id", userId);
    response = new UserResponse(userId, username, email, null, true);
  }

  @Nested
  @DisplayName("login")
  class Login {

    @Test
    @DisplayName("success")
    void login_success() {
      // given
      LoginRequest request = new LoginRequest(username, password);
      given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
      given(mapper.toResponse(any(User.class))).willReturn(response);

      // when
      UserResponse result = authService.login(request);

      // then
      assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("fail with user not found")
    void login_fail_user_not_found_throws_exception() {
      // given
      LoginRequest request = new LoginRequest(username, password);
      given(userRepository.findByUsername(username)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> authService.login(request))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("fail with wrong password")
    void login_fail_wrong_password_throws_exception() {
      // given
      LoginRequest request = new LoginRequest(username, "password");
      given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

      // when & then
      assertThatThrownBy(() -> authService.login(request))
          .isInstanceOf(InvalidCredentialsException.class);
    }
  }
}