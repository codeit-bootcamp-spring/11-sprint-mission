package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private UserMapper userMapper;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JwtRegistry jwtRegistry;

  @InjectMocks
  private BasicUserService userService;

  @Test
  void create_성공() {
    UserCreateRequest request = new UserCreateRequest("jihye", "jihye@test.com", "password123");
    User user = new User("jihye", "jihye@test.com", "password123", null);
    UserDto dto = new UserDto(user.getId(), "jihye", "jihye@test.com", null, false, Role.USER);

    given(passwordEncoder.encode("password123")).willReturn("hashed_password");
    given(userRepository.existsByUsername("jihye")).willReturn(false);
    given(userRepository.existsByEmail("jihye@test.com")).willReturn(false);
    given(userRepository.save(any(User.class))).willReturn(user);
    given(jwtRegistry.hasActiveJwtInformationByUserId(any())).willReturn(false);
    given(userMapper.toDto(any(User.class), anyBoolean())).willReturn(dto);

    UserDto result = userService.create(request, null);

    assertThat(result).isNotNull();
    assertThat(result.username()).isEqualTo("jihye");
  }

  @Test
  void create_실패_중복username() {
    UserCreateRequest request = new UserCreateRequest("jihye", "jihye@test.com", "password123");
    given(userRepository.existsByUsername("jihye")).willReturn(true);

    assertThatThrownBy(() -> userService.create(request, null))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void create_실패_중복email() {
    UserCreateRequest request = new UserCreateRequest("jihye", "jihye@test.com", "password123");
    given(userRepository.existsByUsername("jihye")).willReturn(false);
    given(userRepository.existsByEmail("jihye@test.com")).willReturn(true);

    assertThatThrownBy(() -> userService.create(request, null))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void update_성공() {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("newName", "new@test.com", "newPassword123");
    User user = new User("jihye", "jihye@test.com", "password123", null);
    UserDto dto = new UserDto(userId, "newName", "new@test.com", null, false, Role.USER);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByUsername("newName")).willReturn(false);
    given(userRepository.existsByEmail("new@test.com")).willReturn(false);
    given(jwtRegistry.hasActiveJwtInformationByUserId(any())).willReturn(false);
    given(userMapper.toDto(any(User.class), anyBoolean())).willReturn(dto);

    UserDto result = userService.update(userId, request, null);

    assertThat(result).isNotNull();
    assertThat(result.username()).isEqualTo("newName");
  }

  @Test
  void update_실패_존재하지않는유저() {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("newName", "new@test.com", "newPassword123");
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.update(userId, request, null))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void delete_성공() {
    UUID userId = UUID.randomUUID();
    User user = new User("jihye", "jihye@test.com", "password123", null);
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    userService.delete(userId);

    then(userRepository).should().delete(user);
  }

  @Test
  void delete_실패_존재하지않는유저() {
    UUID userId = UUID.randomUUID();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.delete(userId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void findById_성공() {
    UUID userId = UUID.randomUUID();
    User user = new User("jihye", "jihye@test.com", "password123", null);
    UserDto dto = new UserDto(userId, "jihye", "jihye@test.com", null, false, Role.USER);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(jwtRegistry.hasActiveJwtInformationByUserId(any())).willReturn(false);
    given(userMapper.toDto(any(User.class), anyBoolean())).willReturn(dto);

    UserDto result = userService.findById(userId);

    assertThat(result).isNotNull();
    assertThat(result.username()).isEqualTo("jihye");
  }

  @Test
  void findById_실패_존재하지않는유저() {
    UUID userId = UUID.randomUUID();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(userId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void findAll_성공() {
    User user = new User("jihye", "jihye@test.com", "password123", null);
    UserDto dto = new UserDto(user.getId(), "jihye", "jihye@test.com", null, false, Role.USER);

    given(userRepository.findAll()).willReturn(List.of(user));
    given(jwtRegistry.hasActiveJwtInformationByUserId(any())).willReturn(false);
    given(userMapper.toDto(any(User.class), anyBoolean())).willReturn(dto);

    List<UserDto> result = userService.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).username()).isEqualTo("jihye");
  }
}