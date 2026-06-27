package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.security.UserOnlineStatusResolver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {BinaryContentMapper.class, UserOnlineStatusResolver.class}
)
public interface UserMapper {

    @Mapping(target = "profile", source = "profile")
    @Mapping(target = "online", source = "user", qualifiedByName = "isOnline")
    UserDto toDto(User user);
}
