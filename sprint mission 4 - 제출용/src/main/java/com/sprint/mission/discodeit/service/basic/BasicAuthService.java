package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.DiscodeitInvalidPasswordException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;

  @Override
  public UserResponse login(LoginRequest request) {
    User user = userRepository.findByUserName(request.getUserName());
    if (user == null || !user.getUserPassword().equals(request.getUserPassword())) {
      throw new DiscodeitInvalidPasswordException();
    }
    return new UserResponse(user.getId(), user.getUserName(), user.getUserEmail(), true);
  }
}
