package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.DuplicateEmailException;
import com.sprint.mission.discodeit.exception.user.DuplicateUsernameException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

  @Mock
  UserRepository userRepository;
  @Mock
  UserStatusRepository userStatusRepository;
  @Mock
  UserMapper userMapper;
  @Mock
  BinaryContentRepository binaryContentRepository;
  @Mock
  BinaryContentStorage binaryContentStorage;

  @InjectMocks
  BasicUserService userService;

  // create()
  @Test
  void create_이메일중복_예외발생() {
    UserCreateRequest request = new UserCreateRequest("testuser", "test@test.com", "password");
    given(userRepository.existsByEmail("test@test.com")).willReturn(true);

    assertThatThrownBy(() -> userService.create(request, Optional.empty()))
        .isInstanceOf(DuplicateEmailException.class);
  }

  @Test
  void create_유저명중복_예외발생() {
    UserCreateRequest request = new UserCreateRequest("testuser", "test@test.com", "password");
    given(userRepository.existsByEmail("test@test.com")).willReturn(false);
    given(userRepository.existsByUsername("testuser")).willReturn(true);

    assertThatThrownBy(() -> userService.create(request, Optional.empty()))
        .isInstanceOf(DuplicateUsernameException.class);
  }

  @Test
  void create_정상_유저저장() {
    UserCreateRequest request = new UserCreateRequest("testuser", "test@test.com", "password");
    UserDto userDto = new UserDto(UUID.randomUUID(), "testuser", "test@test.com", null, false);

    given(userRepository.existsByEmail("test@test.com")).willReturn(false);
    given(userRepository.existsByUsername("testuser")).willReturn(false);
    given(userMapper.toDto(any(User.class))).willReturn(userDto);

    UserDto result = userService.create(request, Optional.empty());

    verify(userRepository).save(any(User.class));
    assertThat(result.username()).isEqualTo("testuser");
  }

  // find()
  @Test
  void find_존재하지않는유저_예외발생() {
    UUID userId = UUID.randomUUID();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.find(userId))
        .isInstanceOf(UserNotFoundException.class);
  }

  // update()
  @Test
  void update_존재하지않는유저_예외발생() {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("newuser", "new@test.com", "newpassword");
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.update(userId, request, Optional.empty()))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void update_이메일중복_예외발생() {
    UUID userId = UUID.randomUUID();
    User user = new User("testuser", "test@test.com", "password", null);
    UserUpdateRequest request = new UserUpdateRequest("newuser", "new@test.com", "newpassword");

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByEmail("new@test.com")).willReturn(true);

    assertThatThrownBy(() -> userService.update(userId, request, Optional.empty()))
        .isInstanceOf(DuplicateEmailException.class);
  }

  @Test
  void update_유저명중복_예외발생() {
    UUID userId = UUID.randomUUID();
    User user = new User("testuser", "test@test.com", "password", null);
    UserUpdateRequest request = new UserUpdateRequest("newuser", "new@test.com", "newpassword");

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByEmail("new@test.com")).willReturn(false);
    given(userRepository.existsByUsername("newuser")).willReturn(true);

    assertThatThrownBy(() -> userService.update(userId, request, Optional.empty()))
        .isInstanceOf(DuplicateUsernameException.class);
  }

  // delete()
  @Test
  void delete_존재하지않는유저_예외발생() {
    UUID userId = UUID.randomUUID();
    given(userRepository.existsById(userId)).willReturn(false);

    assertThatThrownBy(() -> userService.delete(userId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void delete_정상_삭제호출() {
    UUID userId = UUID.randomUUID();
    given(userRepository.existsById(userId)).willReturn(true);

    userService.delete(userId);

    verify(userRepository).deleteById(userId);
  }
}