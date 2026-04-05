package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;

  @Override
  public User login(LoginRequest request) {
    // 조건 -> 탐색 -> 조건에 맞지 않으면
    return userRepository.findAll().stream()
        .filter(user -> user.getUsername().equals(request.username()) && user.getPassword()
            .equals(request.password()))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("사용자 이름 또는 비밀번호가 일치하지 않습니다."));
  }

}
