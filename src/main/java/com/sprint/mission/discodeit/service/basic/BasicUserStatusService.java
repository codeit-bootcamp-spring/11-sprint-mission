package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusAlreadyExists;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
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
  public UserStatusDto create(UserStatusCreateRequest dto) {
    User user = userRepository.findById(dto.userId())
        .orElseThrow(
            () -> new UserNotFoundException(dto.userId()));
    if (userStatusRepository.existsByUserId(user.getId())) {
      throw new UserStatusAlreadyExists(user.getId());
    }

    UserStatus userStatus = new UserStatus(user, dto.lastActiveAt());
    userStatus = userStatusRepository.save(userStatus);

    return userStatusMapper.toDto(userStatus);
  }

  @Override
  public UserStatusDto findByUserId(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }

    UserStatus userStatus = userStatusRepository.findByUserId(userId)
        .orElseThrow(
            () -> UserStatusNotFoundException.byUserId(userId));

    return userStatusMapper.toDto(userStatus);
  }

  @Override
  public List<UserStatusDto> findAll() {
    return userStatusRepository.findAll().stream()
        .map(userStatusMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public UserStatusDto update(UUID userId, UserStatusUpdateRequest dto) {
    UserStatus userStatus = userStatusRepository.findByUserId(userId)
        .orElseThrow(
            () -> UserStatusNotFoundException.byUserId(userId));
    userStatus.updateTime(dto.newLastActiveAt());

    return userStatusMapper.toDto(userStatus);
  }

//  @Override
//  public void updateByUserId(UUID userId) {
//    UserStatus userStatus = userStatusRepository.findByUserId(userId)
//        .orElseThrow(
//            () -> new NoSuchElementException("UserStatus with userId " + userId + " not found"));
//
//    userStatus.updateTime(Instant.now());
//    userStatusRepository.save(userStatus);
//  }

  @Override
  @Transactional
  public void delete(UUID id) {
    UserStatus userStatus = userStatusRepository.findById(id)
        .orElseThrow(() -> UserStatusNotFoundException.byStatusId(id));

    userStatusRepository.delete(userStatus);
  }
}
