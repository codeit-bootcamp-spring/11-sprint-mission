package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.stereotype.Component;

@Component
public class BinaryContentMapper {

  public BinaryContentDto toDto(BinaryContent content) {
    if (content == null) {
      return null;
    }

    return new BinaryContentDto(
        content.getId(),
        content.getFileName(),
        content.getContentType(),
        content.getSize()
    );
  }

}
