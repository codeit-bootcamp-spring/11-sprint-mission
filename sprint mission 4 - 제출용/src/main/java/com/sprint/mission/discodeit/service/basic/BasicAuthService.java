package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.auth.LoginDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitInvalidPasswordException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;

  @Override
  public LoginDto login(LoginRequest request) {
    User user = userRepository.findByUsername(request.getUsername())
        .orElseThrow((DiscodeitInvalidPasswordException::new));

    if (!user.getPassword().equals(request.getPassword())) {
      throw new DiscodeitInvalidPasswordException();
    }
    return new LoginDto(user.getId(), user.getUsername(), user.getEmail(), true);
  }
}
