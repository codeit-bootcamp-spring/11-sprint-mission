package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;

  public UserDto login(LoginRequest loginRequest) {
    String name = loginRequest.username();
    String password = loginRequest.password();

    User user = userRepository.findByUserName(name)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    if (!user.getPassword().equals(password.trim())) {
      throw new BusinessLogicException(ExceptionCode.LOGIN_FAILED);
    }
    UserStatus status = userStatusRepository.findByUserId(user.getId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.INTERNAL_SERVER_ERROR));

    status.update(null);

    userStatusRepository.save(status);

    return UserDto.from(user, status);
  }
}
