package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private BasicUserService userService;

  // Create 테스트
  @Test
  @DisplayName("사용자 생성 성공")
  void create_success() {
    // Given
    UserDto.CreateRequest request = UserDto.CreateRequest.builder()
        .username("woody")
        .email("woody@test.com")
        .password("pass1234!")
        .build();
    UserDto.Response mockResponse = UserDto.Response.builder()
        .username(request.username())
        .email(request.email())
        .build();

    given(userRepository.existsByUsername(anyString())).willReturn(false);
    given(userRepository.existsByEmail(anyString())).willReturn(false);
    given(userMapper.toDto(any(User.class))).willReturn(mockResponse);

    // When
    UserDto.Response result = userService.create(request, null);

    // Then
    assertThat(result.username()).isEqualTo(request.username());
    assertThat(result.email()).isEqualTo(request.email());

    then(userRepository).should().save(any(User.class));
  }

  @Test
  @DisplayName("사용자명 중복으로 사용자 생성 실패")
  void create_fail_duplicateUsername() {
    // Given
    UserDto.CreateRequest request = UserDto.CreateRequest.builder()
        .username("duplicate")
        .email("woody@test.com")
        .password("pass1234!")
        .build();

    // username에서 중복 발생
    given(userRepository.existsByUsername(request.username())).willReturn(true);

    // When & Then
    assertThatThrownBy(() -> userService.create(request, null))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  @DisplayName("이메일 중복으로 사용자 생성 실패")
  void create_fail_duplicateEmail() {
    // Given
    UserDto.CreateRequest request = UserDto.CreateRequest.builder()
        .username("woody")
        .email("duplicate@test.com")
        .password("pass1234!")
        .build();

    // email에서 중복 발생
    given(userRepository.existsByUsername(request.username())).willReturn(false);
    given(userRepository.existsByEmail(request.email())).willReturn(true);

    // When & Then
    assertThatThrownBy(() -> userService.create(request, null))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  // Update 테스트
  @Test
  @DisplayName("사용자 수정 성공")
  void update_success() {
    // Given
    UUID userId = UUID.randomUUID();
    User existingUser = User.builder().username("oldWoody").email("old@test.com").build();
    UserDto.UpdateRequest request = new UserDto.UpdateRequest(
        "newWoody", "new@test.com", null
    );
    UserDto.Response mockResponse = UserDto.Response.builder()
        .username(request.newUsername())
        .email(request.newEmail())
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
    given(userRepository.existsByUsername(request.newUsername())).willReturn(false);
    given(userRepository.existsByEmail(request.newEmail())).willReturn(false);

    given(userMapper.toDto(any(User.class))).willReturn(mockResponse);

    // When
    UserDto.Response result = userService.update(userId, request, null);

    // Then
    assertThat(result.username()).isEqualTo(request.newUsername());
    assertThat(existingUser.getUsername()).isEqualTo(request.newUsername());
    assertThat(existingUser.getEmail()).isEqualTo(request.newEmail());
  }

  @Test
  @DisplayName("사용자 수정 성공 - 기존 정보와 완전히 동일한 정보로 수정을 요청하는 경우")
  void update_success_sameValues() {
    // Given
    UUID userId = UUID.randomUUID();
    User existingUser = User.builder()
        .username("woody")
        .email("woody@test.com")
        .password("pass1234!")
        .build();

    UserDto.UpdateRequest updateRequest = new UserDto.UpdateRequest(
        "woody", "woody@test.com", "pass1234!"
    );
    UserDto.Response mockResponse = UserDto.Response.builder()
        .username(existingUser.getUsername())
        .email(existingUser.getEmail())
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
    given(userMapper.toDto(any(User.class))).willReturn(mockResponse);

    // When
    UserDto.Response result = userService.update(userId, updateRequest, null);

    // Then
    assertThat(result.username()).isEqualTo(existingUser.getUsername());
    assertThat(result.email()).isEqualTo(existingUser.getEmail());

    then(userRepository).should().findById(userId);
    then(userRepository).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("존재하지 않는 사용자 수정 시 UserNotFoundException 발생")
  void update_fail_userNotFound() {
    // Given
    UUID notExistingId = UUID.randomUUID();
    UserDto.UpdateRequest updateRequest = new UserDto.UpdateRequest(
        "newWoody", "new@test.com", null
    );

    given(userRepository.findById(notExistingId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.update(notExistingId, updateRequest, null))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @DisplayName("이메일 중복으로 사용자 수정 실패 시 UserAlreadyExistsException 발생")
  void update_fail_duplicateEmail() {
    // Given
    UUID existingId = UUID.randomUUID();
    User existingUser = User.builder()
        .username("oldWoody")
        .email("old@test.com")
        .build();
    UserDto.UpdateRequest updateRequest = new UserDto.UpdateRequest(
        "newWoody", "duplicate@test.com", null
    );

    given(userRepository.findById(existingId)).willReturn(Optional.of(existingUser));
    given(userRepository.existsByUsername(updateRequest.newUsername())).willReturn(false);
    given(userRepository.existsByEmail(updateRequest.newEmail())).willReturn(true);

    // When & Then
    assertThatThrownBy(() -> userService.update(existingId, updateRequest, null))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  // Delete 테스트
  @Test
  @DisplayName("사용자 삭제 성공")
  void delete_success() {
    // Given
    UUID userId = UUID.randomUUID();
    User existingUser = User.builder().username("deleteUser").build();

    given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

    // When
    userService.delete(userId);

    // Then
    then(userRepository).should().delete(existingUser);
  }

  @Test
  @DisplayName("존재하지 않는 사용자 삭제 실패 시 UserNotFoundException 발생")
  void delete_fail_userNotFound() {
    // Given
    UUID notExistingId = UUID.randomUUID();

    given(userRepository.findById(notExistingId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.delete(notExistingId))
        .isInstanceOf(UserNotFoundException.class);

    then(userRepository).shouldHaveNoMoreInteractions();
  }

  // FindById 테스트
  @Test
  @DisplayName("사용자 단건 조회 성공")
  void findById_success() {
    // Given
    UUID userId = UUID.randomUUID();
    User existingUser = User.builder()
        .username("woody")
        .email("woody@test.com")
        .build();
    UserDto.Response mockResponse = UserDto.Response.builder()
        .username(existingUser.getUsername())
        .email(existingUser.getEmail())
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
    given(userMapper.toDto(existingUser)).willReturn(mockResponse);

    // When
    UserDto.Response result = userService.findById(userId);

    // Then
    assertThat(result.username()).isEqualTo(existingUser.getUsername());
    assertThat(result.email()).isEqualTo(existingUser.getEmail());

    then(userRepository).should().findById(userId);
    then(userMapper).should().toDto(existingUser);
  }

  @Test
  @DisplayName("존재하지 않는 사용자 단건 조회 시 UserNotFoundException 발생")
  void findById_fail_userNotFound() {
    // Given
    UUID notExistingId = UUID.randomUUID();

    given(userRepository.findById(notExistingId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.findById(notExistingId))
        .isInstanceOf(UserNotFoundException.class);

    then(userRepository).should().findById(notExistingId);
    then(userMapper).shouldHaveNoInteractions();
  }
}