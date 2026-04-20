package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.User.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  private final BinaryContentMapper binaryContentMapper;

  public UserDto toDto(User user) {
    return new UserDto(
        user.getId(), // id
        user.getUsername(), // username
        user.getEmail(), // email
        user.getProfile() != null ? // profile(BinaryContentDto)
            binaryContentMapper.toDto(user.getProfile()) : null, // 프로필 이미지가 있을수도 없을수도 있음
        user.getStatus() != null // userStatus
            && user.getStatus().status() == Status.ONLINE
        // UserStatus가 존재하고 Status.ONLINE를 가지면 프론트엔드상에서 true(온라인)를, 그렇지 않으면 false(오프라인)를 반환
    );
  }

}
