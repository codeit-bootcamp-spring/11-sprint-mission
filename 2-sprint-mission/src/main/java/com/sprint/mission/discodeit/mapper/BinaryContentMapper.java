package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BinaryContentMapper {

  @Mapping(target = "bytes", ignore = true)
  BinaryContentDto.Response toDto(BinaryContent binaryContent);
}