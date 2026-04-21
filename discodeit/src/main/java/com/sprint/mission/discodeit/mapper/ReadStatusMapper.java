package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.dto.readstatus.ReadStatusDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReadStatusMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "channelId", source = "channel.id")
    ReadStatusDto toDto(ReadStatus readStatus);
}
