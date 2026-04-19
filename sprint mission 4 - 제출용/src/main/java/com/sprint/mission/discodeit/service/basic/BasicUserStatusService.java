package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userStatus.UserStatusDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitDuplicateException;
import com.sprint.mission.discodeit.exception.DiscodeitNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;
  private final UserStatusMapper userStatusMapper;

  @Override
  @Transactional
  public UserStatusDto create(UserStatusCreateRequest request) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> DiscodeitNotFoundException.user(request.getUserId()));

    if (userStatusRepository.findByUser_Id(request.getUserId()).isPresent()) {
      throw DiscodeitDuplicateException.userStatus(request.getUserId());
    }

    UserStatus userStatus = new UserStatus(user, Instant.now());
    UserStatus savedUserStatus = userStatusRepository.save(userStatus);

    return userStatusMapper.toDto(savedUserStatus);
  }

  // userStatus가 아니라 userId 기반으로 찾기
  @Override
  @Transactional(readOnly = true)
  public UserStatusDto findByUserId(UUID userId) {
    UserStatus userStatus = userStatusRepository.findByUser_Id(userId)
        .orElseThrow(() -> DiscodeitNotFoundException.userStatus(userId));

    return userStatusMapper.toDto(userStatus);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserStatusDto> findAll() {
    return userStatusRepository.findAll().stream()
        .map(userStatusMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void update(UserStatusUpdateRequest request) {
    UserStatus userStatus = userStatusRepository.findByUser_Id(request.getUserId())
        .orElseThrow(() -> DiscodeitNotFoundException.userStatus(request.getUserId()));

    userStatus.updateUserStatus(request.getNewLastActiveAt());
  }

  @Override
  @Transactional
  public void delete(UUID userId) {
    UserStatus userStatus = userStatusRepository.findByUser_Id(userId)
        .orElseThrow(() -> DiscodeitNotFoundException.userStatus(userId));

    userStatusRepository.delete(userStatus);
  }
}
