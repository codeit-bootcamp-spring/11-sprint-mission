package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.NoSuchElementException;
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
  public UserStatus create(UserStatusCreateRequest dto) {

    // 관련된 User가 존재하지 않으면 예외를 발생
    User user = userRepository.findById(dto.userId()).orElseThrow(
        () -> new NoSuchElementException("해당 User가 존재하지 않습니다. UserID :" + dto.userId()));

    // 같은 User와 관련된 객체가 이미 존재하면 예외를 발생
    userStatusRepository.findByUserId(dto.userId()).ifPresent(userStatus -> {
      throw new IllegalArgumentException("이미 해당 UserStatus가 존재합니다. User : " + dto.userId());
    });

    UserStatus userStatus = new UserStatus(user, dto.lastActiveAt());
    userStatusRepository.save(userStatus);

    return userStatus;
  }

  @Override
  @Transactional(readOnly = true)
  public UserStatusDto find(UUID id) {
    UserStatus userStatus = userStatusRepository.findById(id).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 UserStatus입니다. id : " + id)
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
  public UserStatus update(UUID id, UserStatusUpdateRequest dto) {
    UserStatus userStatus = userStatusRepository.findById(id).orElseThrow(
        () -> new NoSuchElementException("해당 UserStatus가 존재하지 않습니다. Id : " + id));
    userStatus.updateLastOnline(dto.lastActiveAt());
    userStatusRepository.save(userStatus);

    return userStatus;
  }

  // userId로 UserStatus를 update
  @Override
  @Transactional
  public UserStatus updateByUserId(UUID userId, UserStatusUpdateRequest dto) {
    UserStatus userStatus = userStatusRepository.findByUserId(userId).orElseThrow(
        () -> new NoSuchElementException("해당 User에 대한 UserStatus가 존재하지 않습니다. userId : " + userId));
    userStatus.updateLastOnline(dto.lastActiveAt());
    userStatusRepository.save(userStatus);

    return userStatus;
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!userStatusRepository.existsById(id)) {
      throw new NoSuchElementException("해당 UserStatus는 존재하지 않습니다. UserStatus Id : " + id);
    }
    userStatusRepository.deleteById(id);
  }
}