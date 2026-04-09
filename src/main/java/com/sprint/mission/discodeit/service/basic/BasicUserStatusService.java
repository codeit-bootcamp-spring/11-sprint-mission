package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusCreateDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
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

  @Override
  @Transactional
  public UserStatus createUserStatus(UserStatusCreateDto statusCreateDto) {
    User user = userRepository.findById(statusCreateDto.userId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

    userStatusRepository.findByUserId(statusCreateDto.userId())
        .ifPresent(existStatus -> {
          throw new BusinessLogicException(ExceptionCode.USER_STATUS_EXISTS);
        });

    UserStatus userStatus = new UserStatus(user);
    userStatusRepository.save(userStatus);
    return userStatus;
  }

  @Override
  public UserStatus findUserStatus(UUID id) {
    return userStatusRepository.findByUserId(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_STATUS_NOT_FOUND));
  }

  @Override
  public List<UserStatus> findAllUserStatus() {
    List<UserStatus> allUserStatus = userStatusRepository.findAll();

    return allUserStatus;
  }

  @Override
  @Transactional
  public void deleteUserStatus(UUID id) {
    UserStatus status = userStatusRepository.findByUserId(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_STATUS_NOT_FOUND));

    userStatusRepository.delete(status);
  }

  @Override
  @Transactional
  public UserStatus updateUserIdStatus(UUID userId, UserStatusUpdateDto request) {
    return userStatusRepository.findByUserId(userId)
        .map(status -> {
          status.update(request.newLastActiveAt());
          return status;
        })
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_STATUS_NOT_FOUND));
  }
}
