package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.BDDMockito.willThrow;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BasicUserServiceTest {

  @InjectMocks
  private BasicUserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @Test
  @DisplayName("사용자 생성 성공 - 중복 없는 정상적인 요청")
  void create_success() {
    UserCreateRequest request = new UserCreateRequest("testUser",
        "test@email.com", "password123", null);
    User savedUser = new User("testUser", "test@email.com", "password123");
    UserDto userDto = new UserDto(UUID.randomUUID(),
        "testUser", "test@email.com", null, true);

    given(userRepository.existsByUsername(request.username())).willReturn(false);
    given(userRepository.existsByEmail(request.email())).willReturn(false);
    given(userMapper.toDto(any(User.class))).willReturn(userDto);

    UserDto result = userService.create(request, null);

    assertThat(result.username()).isEqualTo("testUser");
    then(userRepository).should().save(any(User.class));
  }

  @Test
  @DisplayName("사용자 생성 실패 - 이미 존재하는 이메일")
  void create_fail_duplicateEmail() {
    UserCreateRequest request = new UserCreateRequest("testUser",
        "dup@emil.com", "password123", null);

    given(userRepository.existsByUsername(request.username())).willReturn(false);
    given(userRepository.existsByEmail(request.email())).willReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> {
      userService.create(request, null);
    });

    then(userRepository).should(never()).save(any(User.class));
  }

  @Test
  @DisplayName("사용자 수정 성공 - 존재하는 유저 정보 업데이트")
  void update_success() {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest(
        "newUsername", null, null);
    User existingUser = new User("oldUsername", "test@email.com", "password");
    UserDto updatedDto = new UserDto(userId, "newUsername",
        "test@email.com", null, true);

    given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
    given(userMapper.toDto(existingUser)).willReturn(updatedDto);

    UserDto result = userService.update(userId, request, null);

    assertThat(result.username()).isEqualTo("newUsername");
  }

  @Test
  @DisplayName("사용자 수정 실패 - 존재하지 않는 유저 조회")
  void update_fail_userNotFound() {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest(
        "newUsername", null, null);

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
      userService.update(userId, request, null);
    });
  }

  @Test
  @DisplayName("사용자 삭제 성공")
  void delete_success() {
    UUID userId = UUID.randomUUID();

    userService.delete(userId);

    then(userRepository).should().deleteById(userId);
  }

  @Test
  @DisplayName("사용자 삭제 싫패 - 예외 발생 시뮬레이션")
  void delete_fail() {
    UUID userId = UUID.randomUUID();

    willThrow(new RuntimeException("DB Error"))
        .given(userRepository).deleteById(userId);

    assertThrows(RuntimeException.class, () -> {
      userService.delete(userId);
    });
  }
}
