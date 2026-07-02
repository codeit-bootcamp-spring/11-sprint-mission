package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserMapper {

  private final BinaryContentMapper binaryContentMapper;

  private final SessionService sessionService;

  public UserDto toDto(User user) {
    return new UserDto(
        user.getId(), // id
        user.getUsername(), // username
        user.getEmail(), // email
        user.getProfile() != null ? // profile(BinaryContentDto)
            binaryContentMapper.toDto(user.getProfile()) : null, // 프로필 이미지가 있을수도 없을수도 있음
        sessionService.isOnline(user.getId()), // 로그인한 유저는 online, 그렇지 않으면 offline
        user.getRole()
    );
  }

}
