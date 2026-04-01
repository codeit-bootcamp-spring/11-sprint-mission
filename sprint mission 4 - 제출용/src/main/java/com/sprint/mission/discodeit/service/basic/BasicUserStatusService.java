package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitDuplicateException;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.DiscodeitNotFoundException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;

  @Override
  public UserStatus create(UserStatusCreateRequest request) {
    if (userRepository.read(request.getUserId()) == null) {
      throw DiscodeitNotFoundException.user(request.getUserId());
    }
    // 유저는 하나의 상태만 가질 수 있음.
    if (userStatusRepository.readByUserId(request.getUserId()) != null) {
      throw DiscodeitDuplicateException.userStatus(request.getUserId());
    }
    UserStatus userStatus = new UserStatus(request.getUserId(), Instant.now());
    return userStatusRepository.create(userStatus);
  }

  @Override
  public UserStatus read(UUID id) {
    UserStatus userStatus = userStatusRepository.readByUserId(id);
    if (userStatus == null) {
      throw DiscodeitNotFoundException.userStatus(id);
    }
    return userStatus;
  }

  @Override
  public List<UserStatus> readAll() {
    return userStatusRepository.readAll();
  }

  @Override
  public void update(UserStatusUpdateRequest request) {
    UserStatus userStatus = userStatusRepository.readByUserId(request.getUserId());
    if (userStatus == null) {
      throw DiscodeitNotFoundException.userStatus(request.getUserId());
    }
    userStatus.updateLastOnlineAt(request.getNewLastActiveAt());
    userStatusRepository.update(request.getUserId(), request.getNewLastActiveAt());
  }

  @Override
  public void delete(UUID id) {
    userStatusRepository.delete(id);
  }
}
