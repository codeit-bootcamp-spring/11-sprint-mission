package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusDto.UpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;

  @Override
  public UserStatusDto.Response create(UserStatusDto.CreateRequest request) {
    if (!userRepository.existsById(request.userId())) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    // 중복 생성 방지
    boolean isDuplicate = userStatusRepository.findAll().stream()
        .anyMatch(us -> us.getUserId().equals(request.userId()));
    if (isDuplicate) {
      throw new BusinessException(ErrorCode.USER_STATUS_ALREADY_EXISTS);
    }

    UserStatus userStatus = request.toEntity();
    return UserStatusDto.Response.of(userStatusRepository.save(userStatus));
  }

  @Override
  public UserStatusDto.Response findById(UUID id) {
    return userStatusRepository.findById(id)
        .map(UserStatusDto.Response::of)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));
  }

  @Override
  public List<UserStatusDto.Response> findAll() {
    return userStatusRepository.findAll().stream()
        .map(UserStatusDto.Response::of)
        .toList();
  }

  @Override
  public UserStatusDto.Response update(UUID id, UserStatusDto.UpdateRequest request) {
    UserStatus userStatus = userStatusRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

    Instant updateTime = (request != null && request.newLastActiveAt() != null)
        ? request.newLastActiveAt()
        : Instant.now();

    userStatus.updateActiveTime(updateTime);

    return UserStatusDto.Response.of(userStatusRepository.save(userStatus));
  }

  @Override
  public UserStatusDto.Response updateByUserId(UUID userId, UpdateRequest request) {
    UserStatus userStatus = userStatusRepository.findAll().stream()
        .filter(us -> us.getUserId().equals(userId))
        .findFirst()
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

    // request가 비어있으면 현재 시간으로 업데이트
    Instant updateTime = (request != null && request.newLastActiveAt() != null)
        ? request.newLastActiveAt()
        : Instant.now();

    userStatus.updateActiveTime(updateTime);

    return UserStatusDto.Response.of(userStatusRepository.save(userStatus));
  }

  @Override
  public void delete(UUID id) {
    if (!userStatusRepository.existsById(id)) {
      throw new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND);
    }
    userStatusRepository.deleteById(id);
  }
}