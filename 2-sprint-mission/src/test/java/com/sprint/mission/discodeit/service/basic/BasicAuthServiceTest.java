package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.sprint.mission.discodeit.dto.AuthDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.InvalidCredentialsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicAuthServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private BasicAuthService authService;

  // Login 테스트
  @Test
  @DisplayName("로그인 성공")
  void login_success() {
    // Given
    AuthDto.LoginRequest request = new AuthDto.LoginRequest("testuser", "password123!");
    User mockUser = mock(User.class);
    UserDto.Response mockResponse = UserDto.Response.builder()
        .username("testuser")
        .build();

    given(mockUser.getUsername()).willReturn("testuser");
    given(userRepository.findByUsername(request.username())).willReturn(Optional.of(mockUser));
    given(mockUser.matchesPassword(request.password())).willReturn(true);
    given(userMapper.toDto(mockUser)).willReturn(mockResponse);

    // When
    UserDto.Response result = authService.login(request);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.username()).isEqualTo(request.username());

    then(mockUser).should().matchesPassword(request.password());
    then(userRepository).should().findByUsername(request.username());
    then(userMapper).should().toDto(mockUser);
  }

  @Test
  @DisplayName("존재하지 않는 사용자명으로 로그인 실패 시 InvalidCredentialsException 발생")
  void login_fail_userNotFound() {
    // Given
    AuthDto.LoginRequest request = new AuthDto.LoginRequest("nonexistent", "password123!");

    given(userRepository.findByUsername(request.username())).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(InvalidCredentialsException.class);

    then(userRepository).should().findByUsername(request.username());
    then(userMapper).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("비밀번호 불일치로 로그인 실패 시 InvalidCredentialsException 발생")
  void login_fail_wrongPassword() {
    // Given
    AuthDto.LoginRequest request = new AuthDto.LoginRequest("testuser", "wrong_password");
    User user = User.builder()
        .username("testuser")
        .email("test@example.com")
        .password("Password123!")
        .build();

    given(userRepository.findByUsername(request.username())).willReturn(Optional.of(user));

    // When & Then
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(InvalidCredentialsException.class);

    then(userRepository).should().findByUsername(request.username());
    then(userMapper).shouldHaveNoInteractions();
  }
}