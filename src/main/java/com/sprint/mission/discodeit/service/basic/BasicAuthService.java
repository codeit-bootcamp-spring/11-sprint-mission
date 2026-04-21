package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserLoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public UserDto login(UserLoginRequest request) {
    User user = userRepository.findByUsername(request.username()).
        orElseThrow(() -> new IllegalArgumentException("일치하는 유저가 없습니다."));
    if (!user.getPassword().equals(request.password())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
    return userMapper.toDto(user);
  }
}
