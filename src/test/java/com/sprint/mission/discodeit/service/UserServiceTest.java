package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.any;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private BasicUserService userService;

  @Mock
  private UserRepository userRepository;
  @Mock
  private UserStatusRepository userStatusRepository;
  @Mock
  private UserMapper userMapper;
  @Mock
  private BinaryContentRepository binaryContentRepository;
  @Mock
  private BinaryContentStorage binaryContentStorage;

  @Test
  void create_succeeds() {
    UserCreateRequest request = new UserCreateRequest("testuser", "test@example.com", "password123");
    User user = new User("testuser", "test@example.com", "password123", null);
    UserDto expectedDto = new UserDto(UUID.randomUUID(), "testuser", "test@example.com", null, false);

    given(userRepository.existsByEmail("test@example.com")).willReturn(false);
    given(userRepository.existsByUsername("testuser")).willReturn(false);
    given(userRepository.save(any(User.class))).willReturn(user);
    given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

    UserDto result = userService.create(request, Optional.empty());

    assertThat(result).isEqualTo(expectedDto);
    then(userRepository).should().save(any(User.class));
  }

  @Test
  void create_withProfile_succeeds() {
    UserCreateRequest request = new UserCreateRequest("testuser", "test@example.com", "password123");
    BinaryContentCreateRequest profileRequest = new BinaryContentCreateRequest("avatar.png", "image/png", new byte[]{1, 2, 3});
    BinaryContent binaryContent = new BinaryContent("avatar.png", 3L, "image/png");
    User user = new User("testuser", "test@example.com", "password123", binaryContent);
    UserDto expectedDto = new UserDto(UUID.randomUUID(), "testuser", "test@example.com", null, false);

    given(userRepository.existsByEmail("test@example.com")).willReturn(false);
    given(userRepository.existsByUsername("testuser")).willReturn(false);
    given(binaryContentRepository.save(any(BinaryContent.class))).willReturn(binaryContent);
    given(userRepository.save(any(User.class))).willReturn(user);
    given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

    UserDto result = userService.create(request, Optional.of(profileRequest));

    assertThat(result).isEqualTo(expectedDto);
    then(binaryContentRepository).should().save(any(BinaryContent.class));
    then(binaryContentStorage).should().put(any(), any(byte[].class));
  }

  @Test
  void create_withDuplicateEmail_throwsException() {
    UserCreateRequest request = new UserCreateRequest("testuser", "duplicate@example.com", "password123");

    given(userRepository.existsByEmail("duplicate@example.com")).willReturn(true);

    assertThatThrownBy(() -> userService.create(request, Optional.empty()))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void create_withDuplicateUsername_throwsException() {
    UserCreateRequest request = new UserCreateRequest("duplicateuser", "test@example.com", "password123");

    given(userRepository.existsByEmail("test@example.com")).willReturn(false);
    given(userRepository.existsByUsername("duplicateuser")).willReturn(true);

    assertThatThrownBy(() -> userService.create(request, Optional.empty()))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void find_succeeds() {
    UUID userId = UUID.randomUUID();
    User user = new User("testuser", "test@example.com", "password123", null);
    ReflectionTestUtils.setField(user, "id", userId);
    UserDto expectedDto = new UserDto(userId, "testuser", "test@example.com", null, false);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userMapper.toDto(user)).willReturn(expectedDto);

    UserDto result = userService.find(userId);

    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void find_withNonExistentUser_throwsException() {
    UUID userId = UUID.randomUUID();

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.find(userId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void findAll_succeeds() {
    User user1 = new User("user1", "user1@example.com", "password1", null);
    User user2 = new User("user2", "user2@example.com", "password2", null);
    UserDto dto1 = new UserDto(UUID.randomUUID(), "user1", "user1@example.com", null, false);
    UserDto dto2 = new UserDto(UUID.randomUUID(), "user2", "user2@example.com", null, false);

    given(userRepository.findAllWithProfileAndStatus()).willReturn(List.of(user1, user2));
    given(userMapper.toDto(user1)).willReturn(dto1);
    given(userMapper.toDto(user2)).willReturn(dto2);

    List<UserDto> result = userService.findAll();

    assertThat(result).hasSize(2).containsExactly(dto1, dto2);
  }

  @Test
  void update_succeeds() {
    UUID userId = UUID.randomUUID();
    User user = new User("olduser", "old@example.com", "oldpass", null);
    ReflectionTestUtils.setField(user, "id", userId);
    UserUpdateRequest request = new UserUpdateRequest("newuser", "new@example.com", "newpass123");
    UserDto expectedDto = new UserDto(userId, "newuser", "new@example.com", null, false);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByEmail("new@example.com")).willReturn(false);
    given(userRepository.existsByUsername("newuser")).willReturn(false);
    given(userMapper.toDto(user)).willReturn(expectedDto);

    UserDto result = userService.update(userId, request, Optional.empty());

    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void update_withNonExistentUser_throwsException() {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("newuser", "new@example.com", "newpass123");

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.update(userId, request, Optional.empty()))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void update_withDuplicateEmail_throwsException() {
    UUID userId = UUID.randomUUID();
    User user = new User("olduser", "old@example.com", "oldpass", null);
    ReflectionTestUtils.setField(user, "id", userId);
    UserUpdateRequest request = new UserUpdateRequest("newuser", "duplicate@example.com", "newpass123");

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByEmail("duplicate@example.com")).willReturn(true);

    assertThatThrownBy(() -> userService.update(userId, request, Optional.empty()))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void delete_succeeds() {
    UUID userId = UUID.randomUUID();

    given(userRepository.existsById(userId)).willReturn(true);

    userService.delete(userId);

    then(userRepository).should().deleteById(userId);
  }

  @Test
  void delete_withNonExistentUser_throwsException() {
    UUID userId = UUID.randomUUID();

    given(userRepository.existsById(userId)).willReturn(false);

    assertThatThrownBy(() -> userService.delete(userId))
        .isInstanceOf(UserNotFoundException.class);
  }
}