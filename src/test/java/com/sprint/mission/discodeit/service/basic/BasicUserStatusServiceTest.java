package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusAlreadyExistsException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BasicUserStatusServiceTest {

  @InjectMocks
  private BasicUserStatusService userStatusService;

  @Mock
  private UserStatusRepository userStatusRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserStatusMapper userStatusMapper;

  @Test
  @DisplayName("상태 생성 성공")
  void create_success() {
    UserStatusCreateRequest request = new UserStatusCreateRequest(UUID.randomUUID());
    User user = new User("user", "email", "pw");
    UserStatusDto dto = new UserStatusDto(UUID.randomUUID(), request.userId(), Instant.now());

    given(userRepository.findById(request.userId())).willReturn(Optional.of(user));
    given(userStatusRepository.existsByUserId(request.userId())).willReturn(false);
    given(userStatusMapper.toDto(any(UserStatus.class))).willReturn(dto);

    UserStatusDto result = userStatusService.create(request);

    assertThat(result.userId()).isEqualTo(request.userId());
  }

  @Test
  @DisplayName("상태 생성 실패 - 이미 존재하는 상태")
  void create_fail_alreadyExists() {
    UserStatusCreateRequest request = new UserStatusCreateRequest(UUID.randomUUID());
    User user = new User("user", "email", "pw");

    given(userRepository.findById(request.userId())).willReturn(Optional.of(user));
    given(userStatusRepository.existsByUserId(request.userId())).willReturn(true);

    assertThrows(UserStatusAlreadyExistsException.class, () -> {
      userStatusService.create(request);
    });
  }

  @Test
  @DisplayName("유저 ID로 상태 업데이트 실패 - 존재하지 않는 유저")
  void updateByUserId_fail_userNotFound() {
    UUID userId = UUID.randomUUID();
    UserStatusUpdateRequest request = new UserStatusUpdateRequest(Instant.now());

    given(userStatusRepository.findByUserId(userId)).willReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
      userStatusService.updateByUserId(userId, request);
    });
  }
}