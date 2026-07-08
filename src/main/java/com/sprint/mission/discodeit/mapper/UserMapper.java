package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        uses = BinaryContentMapper.class
)
public interface UserMapper {

    @Mapping(target = "profile", source = "user.profile")
    @Mapping(target = "online", expression = "java(onlineUserIds.contains(user.getId()))")
    UserDto toDto(User user, @Context Set<UUID> onlineUserIds);
}
