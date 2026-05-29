package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusAlreadyExistException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;
  private final UserStatusMapper userStatusMapper;

  @Override
  @Transactional
  public UserStatusDto create(UserStatusCreateRequest dto) {

    // 관련된 User가 존재하지 않으면 예외를 발생
    User user = userRepository.findById(dto.userId()).orElseThrow(
        () -> new UserNotFoundException(dto.userId())
    );

    // 같은 User와 관련된 객체가 이미 존재하면 예외를 발생
    userStatusRepository.findByUserId(dto.userId()).ifPresent(userStatus -> {
      throw new UserStatusAlreadyExistException(userStatus.getId());
    });

    UserStatus userStatus = new UserStatus(user, dto.lastActiveAt());
    userStatusRepository.save(userStatus);

    return userStatusMapper.toDto(userStatus);
  }

  @Override
  @Transactional(readOnly = true)
  public UserStatusDto find(UUID id) {
    UserStatus userStatus = userStatusRepository.findById(id).orElseThrow(
        () -> UserStatusNotFoundException.withId(id)
    );
    return userStatusMapper.toDto(userStatus);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserStatusDto> findAll() {
    return userStatusRepository.findAll().stream()
        .map(userStatusMapper::toDto).toList();
  }

  // UserStatusId로 UserStatus를 update
  @Override
  @Transactional
  public UserStatusDto update(UUID id, UserStatusUpdateRequest dto) {
    UserStatus userStatus = userStatusRepository.findById(id).orElseThrow(
        () -> UserStatusNotFoundException.withId(id)
    );
    userStatus.updateLastOnline(dto.newLastActiveAt());
    userStatusRepository.save(userStatus);

    return userStatusMapper.toDto(userStatus);
  }

  // userId로 UserStatus를 update
  @Override
  @Transactional
  public UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest dto) {
    UserStatus userStatus = userStatusRepository.findByUserId(userId).orElseThrow(
        () -> UserStatusNotFoundException.withUserId(userId)
    );
    userStatus.updateLastOnline(dto.newLastActiveAt());
    userStatusRepository.save(userStatus);

    return userStatusMapper.toDto(userStatus);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!userStatusRepository.existsById(id)) {
      throw UserStatusNotFoundException.withId(id);
    }
    userStatusRepository.deleteById(id);
  }
}