package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
    BinaryContentMapper.class,
    UserStatusMapper.class,
    UserOnlineStatusMapper.class
})
public interface UserMapper {

  // SessionRegistry에 만료되지 않은 세션이 있는지 확인
  @Mapping(target = "online", source = "user", qualifiedByName = "online")
  UserDto toDto(User user);
}
