package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.AuthenticationException;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;

  @Override
  @Transactional
  public UserDto login(LoginRequest request) {
    User user = userRepository.findByUsername(request.username())
        .orElseThrow(() -> new NotFoundException(
            "User with username " + request.username() + " not found"));

    if (!user.getPassword().equals(request.password())) {
      throw new AuthenticationException("Wrong password");
    }

    UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
        .orElseThrow(() -> new NotFoundException(
            "UserStatus with userId " + user.getId() + " not found"));
    userStatus.update(Instant.now());

    BinaryContentDto profileDto = null;
    if (user.getProfile() != null) {
      BinaryContent profile = user.getProfile();
      profileDto = new BinaryContentDto(
          profile.getId(),
          profile.getFileName(),
          profile.getSize(),
          profile.getContentType()
      );
    }

    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        profileDto,  // ← 변경!
        userStatus.isOnline()
    );
  }
}