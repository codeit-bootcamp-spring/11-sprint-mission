package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserLoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;

  @Override
  public UserDto login(UserLoginRequest request) {
    User user = userRepository.findByUsername(request.username()).
        orElseThrow(() -> new IllegalArgumentException("일치하는 유저가 없습니다."));
    if (!user.getPassword().equals(request.password())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
    return new UserDto(user.getId(), user.getCreatedAt(), user.getUpdatedAt(),
        user.getUsername(), user.getEmail(), user.getProfileId(),
        true);
  }
}
