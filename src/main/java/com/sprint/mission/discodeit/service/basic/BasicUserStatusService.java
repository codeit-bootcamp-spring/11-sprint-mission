package com.sprint.mission.discodeit.service.basic;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_NOT_FOUND;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_STATUS_DUPLICATED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_STATUS_NOT_FOUND;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.ApiException;
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
    User user = this.userRepository.findById(userStatusCreateRequest.userId())
        .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

    if (this.userStatusRepository.existsByUser(user)) {
      throw new ApiException(USER_STATUS_DUPLICATED);
    }

    UserStatus userStatus = new UserStatus(user);
    this.userStatusRepository.save(userStatus);

    log.info("user status has been created successfully. ✅ [ID: {}]", userStatus.getId());
    log.info("-> {user: {}}", user.getId());
    return this.mapper.toResponse(userStatus);
  }

  @Override
  public UserStatusResponse findById(UUID id) {
    return this.mapper.toResponse(this.userStatusRepository.findById(id)
        .orElseThrow(() -> new ApiException(USER_STATUS_NOT_FOUND)));
  }

  @Override
  public List<UserStatusResponse> findAll() {
    return this.userStatusRepository.findAll().stream()
        .map(this.mapper::toResponse)
        .toList();
  }

  @Transactional
  @Override
  public UserStatusResponse updateUserStatusByUserId(UUID userId,
      UserStatusUpdateRequest userStatusUpdateRequest) {
    UserStatus userStatus = this.userStatusRepository.findByUserId(userId)
        .orElseThrow(() -> new ApiException(USER_STATUS_NOT_FOUND));

    userStatus.updateLastActiveAt(userStatusUpdateRequest.newLastActiveAt());

    log.info("UserStatus has been updated successfully. ✅ [ID: {}]", userStatus.getId());
    log.info("-> {user: {}}", userId);
    return this.mapper.toResponse(userStatus);
  }

  @Transactional
  @Override
  public void deleteUserStatus(UUID id) {
    UserStatus userStatus = this.userStatusRepository.findById(id)
        .orElseThrow(() -> new ApiException(USER_STATUS_NOT_FOUND));

    this.userStatusRepository.delete(userStatus);

    log.info("UserStatus has been deleted successfully. ✅ [ID: {}]", userStatus.getId());
  }
}
