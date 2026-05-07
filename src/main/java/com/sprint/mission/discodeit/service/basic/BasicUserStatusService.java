package com.sprint.mission.discodeit.service.basic;

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
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;
  private final UserStatusMapper mapper;

  @Transactional
  @Override
  public UserStatusResponse createUserStatus(UserStatusCreateRequest userStatusCreateRequest) {
    log.debug("user-status create trial: {}", userStatusCreateRequest);
    User user = this.userRepository.findById(userStatusCreateRequest.userId())
        .orElseThrow(() -> UserNotFoundException.withId(userStatusCreateRequest.userId()));

    if (this.userStatusRepository.existsByUser(user)) {
      throw DuplicateUserStatusException.withUserId(user.getId());
    }

    UserStatus userStatus = new UserStatus(user);
    this.userStatusRepository.save(userStatus);

    log.info("user-status create success: id={}, userId={}", userStatus.getId(), user.getId());
    return this.mapper.toResponse(userStatus);
  }

  @Override
  public UserStatusResponse findById(UUID id) {
    log.debug("user-status find-by-id trial: id={}", id);
    UserStatus userStatus = this.userStatusRepository.findById(id)
        .orElseThrow(() -> UserStatusNotFoundException.withId(id));

    log.info("user-status find-by-id success: id={}", userStatus.getId());
    return this.mapper.toResponse(userStatus);
  }

  @Override
  public List<UserStatusResponse> findAll() {
    log.debug("user-status find-all trial");
    List<UserStatus> userStatuses = this.userStatusRepository.findAll();

    log.info("user-status find-all success: count={}", userStatuses.size());
    return userStatuses.stream()
        .map(this.mapper::toResponse)
        .toList();
  }

  @Transactional
  @Override
  public UserStatusResponse updateUserStatusByUserId(UUID userId,
      UserStatusUpdateRequest userStatusUpdateRequest) {
    log.debug("user-status update trial: userId={}, request={}", userId, userStatusUpdateRequest);
    UserStatus userStatus = this.userStatusRepository.findByUserId(userId)
        .orElseThrow(() -> UserStatusNotFoundException.withUserId(userId));

    userStatus.updateLastActiveAt(userStatusUpdateRequest.newLastActiveAt());

    log.info("user-status update success: id={}, userId={}", userStatus.getId(), userId);
    return this.mapper.toResponse(userStatus);
  }

  @Transactional
  @Override
  public void deleteUserStatus(UUID id) {
    log.debug("user-status delete trial: id={}", id);
    UserStatus userStatus = this.userStatusRepository.findById(id)
        .orElseThrow(() -> UserStatusNotFoundException.withId(id));

    this.userStatusRepository.delete(userStatus);

    log.info("user-status delete success: id={}", userStatus.getId());
  }
}