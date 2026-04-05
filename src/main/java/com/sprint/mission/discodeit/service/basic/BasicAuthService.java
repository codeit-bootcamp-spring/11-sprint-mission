package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.authDto.AuthDto;
import com.sprint.mission.discodeit.dto.userdto.CreatedUserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.service.DiffPasswordException;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  public CreatedUserDto login(AuthDto authDto) throws DiffPasswordException {

    User user = userRepository.getUserByNickname(authDto.username())
        .orElseThrow(() -> new NonExistException("유저 이름 또는 비밀번호가 틀립니다."));
    CreatedUserDto userInfo;
    if (user.checkSamePassword(authDto.password())) {

      userInfo = new CreatedUserDto(
          user.getId(),
          user.getCreatedAt(),
          user.getUpdatedAt(),
          user.getNickname(),
          user.getEmail(),
          user.getPassword(),
          user.getProfileId()
      );

      user.updateUpdatedAt();
      UserStatus userStatus = userStatusRepository.getUserStatus(user.getId()).orElseThrow();
      userStatus.updateLastActiveAt(Instant.now());

      userStatusRepository.saveUserStatus(userStatus);
      userRepository.saveUser(user);


    } else {
      throw new DiffPasswordException();
    }

    return userInfo;
  }


}













