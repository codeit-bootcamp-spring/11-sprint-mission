package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.dto.userstatus.UserStatusDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserStatusMapper {

    @Mapping(target = "userId", source = "user.id")
    UserStatusDto toDto(UserStatus userStatus);
}
