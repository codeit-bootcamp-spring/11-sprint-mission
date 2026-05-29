package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
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
class BasicUserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private UserMapper mapper;

  @InjectMocks
  private BasicUserService userService;

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
  @DisplayName("create user")
  class CreateUser {

    @Test
    @DisplayName("success")
    void createUser_success() {
      // given
      UserCreateRequest request = new UserCreateRequest(username, email, password);
      given(userRepository.existsByUsername(username)).willReturn(false);
      given(userRepository.existsByEmail(email)).willReturn(false);
      given(mapper.toResponse(any(User.class))).willReturn(response);

      // when
      UserResponse result = userService.createUser(request, Optional.empty());

      // then
      assertThat(result).isEqualTo(response);
      then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("fail with duplicate username")
    void createUser_fail_duplicate_username_throws_exception() {
      // given
      UserCreateRequest request = new UserCreateRequest(username, email, password);
      given(userRepository.existsByUsername(username)).willReturn(true);

      // when & then
      assertThatThrownBy(() -> userService.createUser(request, Optional.empty()))
          .isInstanceOf(DuplicateUserException.class);
    }

    @Test
    @DisplayName("fail with duplicate email")
    void createUser_fail_duplicate_email_throws_exception() {
      // given
      UserCreateRequest request = new UserCreateRequest(username, email, password);
      given(userRepository.existsByUsername(username)).willReturn(false);
      given(userRepository.existsByEmail(email)).willReturn(true);

      // when & then
      assertThatThrownBy(() -> userService.createUser(request, Optional.empty()))
          .isInstanceOf(DuplicateUserException.class);
    }
  }

  @Nested
  @DisplayName("find by id")
  class FindById {

    @Test
    @DisplayName("success")
    void findById_success() {
      // given
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(mapper.toResponse(any(User.class))).willReturn(response);

      // when
      UserResponse result = userService.findById(userId);

      // then
      assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("fail with user not found")
    void findById_fail_user_not_found_throws_exception() {
      // given
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.findById(userId))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("find all")
  class FindAll {

    @Test
    @DisplayName("success with results")
    void findAll_success() {
      // given
      given(userRepository.findAll()).willReturn(List.of(user));
      given(mapper.toResponse(user)).willReturn(response);

      // when
      List<UserResponse> result = userService.findAll();

      // then
      assertThat(result).hasSize(1);
      assertThat(result.get(0)).isEqualTo(response);
    }

    @Test
    @DisplayName("success with empty list")
    void findAll_success_empty() {
      // given
      given(userRepository.findAll()).willReturn(List.of());

      // when
      List<UserResponse> result = userService.findAll();

      // then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("update user")
  class UpdateUser {

    @Test
    @DisplayName("success")
    void updateUser_success() {
      // given
      String newUsername = "newUsername";
      String newEmail = "newEmail";
      String newPassword = "newPassword";

      UserUpdateRequest request = new UserUpdateRequest(newUsername, newEmail, newPassword);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(userRepository.existsByUsername(newUsername)).willReturn(false);
      given(userRepository.existsByEmail(newEmail)).willReturn(false);
      given(mapper.toResponse(any(User.class))).willReturn(response);

      // when
      UserResponse result = userService.updateUser(userId, request, Optional.empty());

      // then
      assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("fail with user not found")
    void updateUser_fail_user_not_found_throws_exception() {
      // given
      UserUpdateRequest request = new UserUpdateRequest("newUsername", "newEmail", "newPassword");
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.updateUser(userId, request, Optional.empty()))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("fail with duplicate username")
    void updateUser_fail_duplicate_username_throws_exception() {
      // given
      String newUsername = "newUsername";

      UserUpdateRequest request = new UserUpdateRequest(newUsername, "newEmail", "newPassword");
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(userRepository.existsByUsername(newUsername)).willReturn(true);

      // when & then
      assertThatThrownBy(() -> userService.updateUser(userId, request, Optional.empty()))
          .isInstanceOf(DuplicateUserException.class);
    }

    @Test
    @DisplayName("fail with duplicate email")
    void updateUser_fail_duplicate_email_throws_exception() {
      // given
      String newUsername = "newUsername";
      String newEmail = "newEmail";

      UserUpdateRequest request = new UserUpdateRequest(newUsername, newEmail, "newPassword");
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(userRepository.existsByUsername(newUsername)).willReturn(false);
      given(userRepository.existsByEmail(newEmail)).willReturn(true);

      // when & then
      assertThatThrownBy(() -> userService.updateUser(userId, request, Optional.empty()))
          .isInstanceOf(DuplicateUserException.class);
    }
  }

  @Nested
  @DisplayName("delete user")
  class DeleteUser {

    @Test
    @DisplayName("success")
    void deleteUser_success() {
      // given
      given(userRepository.findById(userId)).willReturn(Optional.of(user));

      // when
      userService.deleteUser(userId);

      // then
      then(readStatusRepository).should().deleteAllByUser(user);
      then(userRepository).should().delete(user);
    }

    @Test
    @DisplayName("fail with user not found")
    void deleteUser_fail_user_not_found_throws_exception() {
      // given
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.deleteUser(userId))
          .isInstanceOf(UserNotFoundException.class);
    }
  }
}