package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusDto.UpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.DuplicateUserStatusException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
  public UserStatusDto.Response create(UserStatusDto.CreateRequest request) {
    log.debug("사용자 상태 생성 시작: userId={}", request.userId());

    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> UserNotFoundException.withId(request.userId()));

    Optional.ofNullable(user.getStatus())
        .ifPresent(status -> {
          throw DuplicateUserStatusException.withUserId(request.userId());
        });

    UserStatus userStatus = new UserStatus(user, request.lastActiveAt());
    userStatusRepository.save(userStatus);

    log.info("사용자 상태 생성 완료: statusId={}, userId={}", userStatus.getId(), request.userId());
    return userStatusMapper.toDto(userStatus);
  }

  @Override
  public UserStatusDto.Response findById(UUID id) {
    log.debug("사용자 상태 단건 조회 시작: id={}", id);

    UserStatusDto.Response response = userStatusRepository.findById(id)
        .map(userStatusMapper::toDto)
        .orElseThrow(() -> UserStatusNotFoundException.withId(id));

    log.info("사용자 상태 단건 조회 완료: id={}", id);
    return response;
  }

  @Override
  public List<UserStatusDto.Response> findAll() {
    log.debug("사용자 상태 전체 조회 시작");

    List<UserStatusDto.Response> responses = userStatusRepository.findAll().stream()
        .map(userStatusMapper::toDto)
        .toList();

    log.info("사용자 상태 전체 조회 완료: 총 {}건", responses.size());
    return responses;
  }


  @Override
  @Transactional
  public UserStatusDto.Response update(UUID id, UserStatusDto.UpdateRequest request) {
    log.debug("사용자 상태 업데이트 시작: id={}", id);

    UserStatus userStatus = userStatusRepository.findById(id)
        .orElseThrow(() -> UserStatusNotFoundException.withId(id));

    Instant updateTime = (request != null && request.newLastActiveAt() != null)
        ? request.newLastActiveAt()
        : Instant.now();
    userStatus.updateActiveTime(updateTime);
    userStatusRepository.save(userStatus);

    log.info("사용자 상태 업데이트 완료: statusId={}", id);
    return userStatusMapper.toDto(userStatus);
  }


  @Transactional
  @Override
  public UserStatusDto.Response updateByUserId(UUID userId, UpdateRequest request) {
    log.debug("사용자 ID 기반 상태 업데이트 시작: userId={}", userId);

    UserStatus userStatus = userStatusRepository.findByUserId(userId)
        .orElseThrow(() -> UserStatusNotFoundException.withUserId(userId));

    Instant newLastActiveAt =
        request.newLastActiveAt() != null ? request.newLastActiveAt() : Instant.now();
    userStatus.updateActiveTime(newLastActiveAt);

    log.info("사용자 ID 기반 상태 업데이트 완료: userId={}", userId);
    return userStatusMapper.toDto(userStatus);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    log.debug("사용자 상태 삭제 시작: id={}", id);

    if (!userStatusRepository.existsById(id)) {
      throw UserStatusNotFoundException.withId(id);
    }
    
    userStatusRepository.deleteById(id);
    log.info("사용자 상태 삭제 완료: statusId={}", id);
  }
}