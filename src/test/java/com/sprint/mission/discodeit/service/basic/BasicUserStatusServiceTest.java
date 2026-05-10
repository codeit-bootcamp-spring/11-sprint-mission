package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.DuplicateUserStatusException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import java.time.Instant;
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
class BasicUserStatusServiceTest {

  @Mock
  private UserStatusRepository userStatusRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserStatusMapper mapper;

  @InjectMocks
  private BasicUserStatusService userStatusService;

  private UUID userStatusId;
  private UUID userId;
  private User user;
  private UserStatus userStatus;
  private UserStatusResponse response;

  @BeforeEach
  void setUp() {
    userStatusId = UUID.randomUUID();
    userId = UUID.randomUUID();
    user = new User("tester", "tester@example.io", "qwerty", null);
    ReflectionTestUtils.setField(user, "id", userId);
    userStatus = new UserStatus(user);
    ReflectionTestUtils.setField(userStatus, "id", userStatusId);
    response = new UserStatusResponse(userStatusId, userId, Instant.now());
  }

  @Nested
  @DisplayName("create user status")
  class CreateUserStatus {

    @Test
    @DisplayName("success")
    void createUserStatus_success() {
      // given
      UserStatusCreateRequest request = new UserStatusCreateRequest(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(userStatusRepository.existsByUser(user)).willReturn(false);
      given(mapper.toResponse(any(UserStatus.class))).willReturn(response);

      // when
      UserStatusResponse result = userStatusService.createUserStatus(request);

      // then
      assertThat(result).isEqualTo(response);
      then(userStatusRepository).should().save(any(UserStatus.class));
    }

    @Test
    @DisplayName("fail with user not found")
    void createUserStatus_fail_user_not_found_throws_exception() {
      // given
      UserStatusCreateRequest request = new UserStatusCreateRequest(userId);
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userStatusService.createUserStatus(request))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("fail with duplicate user status")
    void createUserStatus_fail_duplicate_throws_exception() {
      // given
      UserStatusCreateRequest request = new UserStatusCreateRequest(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(userStatusRepository.existsByUser(user)).willReturn(true);

      // when & then
      assertThatThrownBy(() -> userStatusService.createUserStatus(request))
          .isInstanceOf(DuplicateUserStatusException.class);
    }
  }

  @Nested
  @DisplayName("find by id")
  class FindById {

    @Test
    @DisplayName("success")
    void findById_success() {
      // given
      given(userStatusRepository.findById(userStatusId)).willReturn(Optional.of(userStatus));
      given(mapper.toResponse(userStatus)).willReturn(response);

      // when
      UserStatusResponse result = userStatusService.findById(userStatusId);

      // then
      assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("fail with user status not found")
    void findById_fail_not_found_throws_exception() {
      // given
      given(userStatusRepository.findById(userStatusId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userStatusService.findById(userStatusId))
          .isInstanceOf(UserStatusNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("find all")
  class FindAll {

    @Test
    @DisplayName("success")
    void findAll_success() {
      // given
      given(userStatusRepository.findAll()).willReturn(List.of(userStatus));
      given(mapper.toResponse(userStatus)).willReturn(response);

      // when
      List<UserStatusResponse> result = userStatusService.findAll();

      // then
      assertThat(result).hasSize(1);
      assertThat(result.get(0)).isEqualTo(response);
    }
  }

  @Nested
  @DisplayName("update user status by user id")
  class UpdateUserStatusByUserId {

    @Test
    @DisplayName("success")
    void updateUserStatusByUserId_success() {
      // given
      Instant newLastActiveAt = Instant.now();
      UserStatusUpdateRequest request = new UserStatusUpdateRequest(newLastActiveAt);
      given(userStatusRepository.findByUserId(userId)).willReturn(Optional.of(userStatus));
      given(mapper.toResponse(userStatus)).willReturn(response);

      // when
      UserStatusResponse result = userStatusService.updateUserStatusByUserId(userId, request);

      // then
      assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("fail with user status not found")
    void updateUserStatusByUserId_fail_not_found_throws_exception() {
      // given
      UserStatusUpdateRequest request = new UserStatusUpdateRequest(Instant.now());
      given(userStatusRepository.findByUserId(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userStatusService.updateUserStatusByUserId(userId, request))
          .isInstanceOf(UserStatusNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("delete user status")
  class DeleteUserStatus {

    @Test
    @DisplayName("success")
    void deleteUserStatus_success() {
      // given
      given(userStatusRepository.findById(userStatusId)).willReturn(Optional.of(userStatus));

      // when
      userStatusService.deleteUserStatus(userStatusId);

      // then
      then(userStatusRepository).should().delete(userStatus);
    }

    @Test
    @DisplayName("fail with user status not found")
    void deleteUserStatus_fail_not_found_throws_exception() {
      // given
      given(userStatusRepository.findById(userStatusId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userStatusService.deleteUserStatus(userStatusId))
          .isInstanceOf(UserStatusNotFoundException.class);
    }
  }
}
