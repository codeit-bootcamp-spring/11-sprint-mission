package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.dto.binarycontent.BinaryContentDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BinaryContentMapper {
    BinaryContentDto toDto(BinaryContent binaryContent);
}
