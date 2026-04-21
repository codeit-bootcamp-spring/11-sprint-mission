package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
  public UserStatusDto create(UserStatusCreateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    if (userStatusRepository.existsByUserId(request.userId())) {
      throw new IllegalArgumentException("이미 해당 사용자의 상태 정보가 존재합니다.");
    }
    UserStatus userStatus = new UserStatus(user);
    userStatusRepository.save(userStatus);
    return userStatusMapper.toDto(userStatus);
  }

  @Override
  public UserStatusDto findById(UUID id) {
    UserStatus userStatus = userStatusRepository.findById(id).orElseThrow(
        () -> new IllegalArgumentException("상태 정보를 찾을 수 없습니다."));
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
        () -> new IllegalArgumentException("상태 정보를 찾을 수 없습니다."));
    userStatus.update(request.newLastActiveAt());
  }

  @Override
  @Transactional
  public UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request) {
    UserStatus userStatus = userStatusRepository.findByUserId(userId).orElseThrow(
        () -> new IllegalArgumentException("해당 사용자의 상태 정보를 찾을 수 없습니다."));
    userStatus.update(request.newLastActiveAt());
    return userStatusMapper.toDto(userStatus);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    userStatusRepository.deleteById(id);
  }
}
