package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.stereotype.Component;

@Component
public class BinaryContentMapper {

  public BinaryContentDto toDto(BinaryContent binaryContent) {
    return new BinaryContentDto(
        binaryContent.getId(), // id
        binaryContent.getFileName(), // fileName
        binaryContent.getSize(), // size
        binaryContent.getContentType(), // contentType
        binaryContent.getStatus() // status
//        binaryContent.getBytes() // bytes
    );
  }

}
