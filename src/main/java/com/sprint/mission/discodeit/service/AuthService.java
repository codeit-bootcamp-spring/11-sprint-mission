package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;

  private final UserMapper userMapper;

  @Transactional(readOnly = true)
  public UserDto login(LoginRequest loginRequest) {
    String name = loginRequest.username();
    String password = loginRequest.password();

    User user = userRepository.findByUsername(name)
        .orElseThrow(() -> new DiscodeitException(ErrorCode.MEMBER_NOT_FOUND));
    if (!user.getPassword().equals(password.trim())) {
      throw new DiscodeitException(ErrorCode.LOGIN_FAILED);
    }
    UserStatus status = userStatusRepository.findByUserId(user.getId())
        .orElseThrow(() -> new DiscodeitException(ErrorCode.INTERNAL_SERVER_ERROR));

    status.update(null);

    userStatusRepository.save(status);

    return userMapper.toDto(user, status);
  }
}
