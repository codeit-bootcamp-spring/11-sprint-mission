package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.NoSuchElementException;
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
            () -> new NoSuchElementException("User with id " + dto.userId() + " not found"));
    if (userStatusRepository.existsByUserId(user.getId())) {
      throw new IllegalArgumentException(
          "UserStatus with userId " + user.getId() + " already exists");
    }

    UserStatus userStatus = new UserStatus(user, dto.lastActiveAt());
    userStatus = userStatusRepository.save(userStatus);

    return userStatusMapper.toDto(userStatus);
  }

  @Override
  public UserStatusDto findByUserId(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new NoSuchElementException("User with id " + userId + " not found");
    }

    UserStatus userStatus = userStatusRepository.findByUserId(userId)
        .orElseThrow(
            () -> new NoSuchElementException("UserStatus with userId " + userId + " not found"));

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
            () -> new NoSuchElementException("UserStatus with id " + userId + " not found"));
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
        .orElseThrow(() -> new NoSuchElementException("UserStatus with id " + id + " not found"));

    userStatusRepository.delete(userStatus);
  }
}
