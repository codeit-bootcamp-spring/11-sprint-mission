package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;
  private final UserStatusMapper userStatusMapper;

  @Override
  @Transactional
  public UserStatusDto create(UserStatusCreateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> {
          log.warn("상태 생성 실패(존재하지 않는 유저) - userId: {}", request.userId());
          return new UserNotFoundException(request.userId());
        });
    if (userStatusRepository.existsByUserId(request.userId())) {
      log.warn("상태 생성 실패(이미 존재하는 상태) - userId: {}", request.userId());
      throw new UserStatusAlreadyExistsException(request.userId());
    }
    UserStatus userStatus = new UserStatus(user);
    userStatusRepository.save(userStatus);
    return userStatusMapper.toDto(userStatus);
  }

  @Override
  public UserStatusDto findById(UUID id) {
    UserStatus userStatus = userStatusRepository.findById(id).orElseThrow(
        () -> {
          log.warn("상태 조회 실패(존재하지 않는 상태) - id: {}", id);
          return new UserStatusNotFoundException(id);
        });
    return userStatusMapper.toDto(userStatus);
  }

  @Override
  public List<UserStatusDto> findAll() {
    return userStatusRepository.findAll().stream().
        map(userStatusMapper::toDto).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void update(UUID id, UserStatusUpdateRequest request) {
    UserStatus userStatus = userStatusRepository.findById(id).orElseThrow(
        () -> {
          log.warn("상태 수정 실패(존재하지 않는 상태) - id: {}", id);
          return new UserStatusNotFoundException(id);
        });
    userStatus.update(request.newLastActiveAt());
  }

  @Override
  @Transactional
  public UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request) {
    UserStatus userStatus = userStatusRepository.findByUserId(userId).orElseThrow(
        () -> {
          log.warn("상태 수정 실패(존재하지 않는 유저) - userId: {}", userId);
          return new UserNotFoundException(userId);
        });
    userStatus.update(request.newLastActiveAt());
    return userStatusMapper.toDto(userStatus);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    userStatusRepository.deleteById(id);
  }
}
