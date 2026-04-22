package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.authDto.LoginRequest;
import com.sprint.mission.discodeit.dto.userdto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.service.DiffPasswordException;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.repository.JPAUserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final JPAUserRepository userRepository;
  private final JPAUserStatusRepository userStatusRepository;
  private final BinaryContentMapper binaryContentMapper;

  @Transactional
  @Override
  public UserDto login(LoginRequest loginRequest) throws DiffPasswordException {

    User user = userRepository.findByUsername(loginRequest.username())
        .orElseThrow(() -> new NonExistException("유저 이름 또는 비밀번호가 틀립니다."));

    UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
        .orElseThrow();

    UserDto userInfo;
    if (user.checkSamePassword(loginRequest.password())) {

      userInfo = new UserDto(
          user.getId(),
          user.getUsername(),
          user.getEmail(),
          binaryContentMapper.toDto(user.getProfile()),
          userStatus.isOnline()
      );

      userStatus.updateLastActiveAt(Instant.now());

    } else {
      throw new DiffPasswordException();
    }

    return userInfo;
  }


}













