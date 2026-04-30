package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserStatusNotFoundException;
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

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;

  private final UserStatusMapper userStatusMapper;

  @Override
  @Transactional
  public UserStatusDto createUserStatus(UserStatusCreateRequest statusCreateDto) {
    User user = userRepository.findById(statusCreateDto.userId())
        .orElseThrow(() -> new UserNotFoundException(statusCreateDto.userId()));

    userStatusRepository.findByUserId(statusCreateDto.userId())
        .ifPresent(existStatus -> {
          throw new UserStatusAlreadyExistsException(statusCreateDto.userId());
        });

    UserStatus userStatus = new UserStatus(user);
    userStatusRepository.save(userStatus);
    return userStatusMapper.toDto(userStatus);
  }

  @Override
  public UserStatusDto findUserStatus(UUID userId) {
    UserStatus status = userStatusRepository.findByUserId(userId)
        .orElseThrow(() -> new UserStatusNotFoundException(userId));

    return userStatusMapper.toDto(status);
  }

  @Override
  public List<UserStatusDto> findAllUserStatus() {
    return userStatusRepository.findAll().stream()
        .map(userStatusMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void deleteUserStatus(UUID userId) {
    UserStatus status = userStatusRepository.findByUserId(userId)
        .orElseThrow(() -> new UserStatusNotFoundException(userId));

    userStatusRepository.delete(status);
  }

  @Override
  @Transactional
  public UserStatusDto updateUserIdStatus(UUID userId, UserStatusUpdateDto request) {
    UserStatus userStatus = userStatusRepository.findByUserId(userId)
        .map(status -> {
          status.update(request.newLastActiveAt());
          return status;
        })
        .orElseThrow(() -> new UserStatusNotFoundException(userId));

    return userStatusMapper.toDto(userStatus);
  }
}
