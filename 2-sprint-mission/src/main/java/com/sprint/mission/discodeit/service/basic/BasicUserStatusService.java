package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusDto.UpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;
  private final UserStatusMapper userStatusMapper;

  @Override
  @Transactional
  public UserStatusDto.Response create(UserStatusDto.CreateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    Optional.ofNullable(user.getStatus())
        .ifPresent(status -> {
          throw new BusinessException(ErrorCode.USER_STATUS_ALREADY_EXISTS);
        });

    UserStatus userStatus = new UserStatus(user, request.lastActiveAt());
    userStatusRepository.save(userStatus);

    return userStatusMapper.toDto(userStatus);
  }

  @Override
  public UserStatusDto.Response findById(UUID id) {
    return userStatusRepository.findById(id)
        .map(userStatusMapper::toDto)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));
  }

  @Override
  public List<UserStatusDto.Response> findAll() {
    return userStatusRepository.findAll().stream()
        .map(userStatusMapper::toDto)
        .toList();
  }


  @Override
  @Transactional
  public UserStatusDto.Response update(UUID id, UserStatusDto.UpdateRequest request) {
    UserStatus userStatus = userStatusRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

    Instant updateTime = (request != null && request.newLastActiveAt() != null)
        ? request.newLastActiveAt()
        : Instant.now();
    userStatus.updateActiveTime(updateTime);
    userStatusRepository.save(userStatus);

    return userStatusMapper.toDto(userStatus);
  }


  @Transactional
  @Override
  public UserStatusDto.Response updateByUserId(UUID userId, UpdateRequest request) {
    UserStatus userStatus = userStatusRepository.findByUserId(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

    Instant newLastActiveAt =
        request.newLastActiveAt() != null ? request.newLastActiveAt() : Instant.now();
    userStatus.updateActiveTime(newLastActiveAt);

    return userStatusMapper.toDto(userStatus);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!userStatusRepository.existsById(id)) {
      throw new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND);
    }
    userStatusRepository.deleteById(id);
  }
}