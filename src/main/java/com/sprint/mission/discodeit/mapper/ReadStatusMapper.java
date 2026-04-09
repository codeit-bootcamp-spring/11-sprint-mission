package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.stereotype.Component;

@Component
public class ReadStatusMapper {

  public ReadStatusDto toDto(ReadStatus status) {
    if (status == null) {
      return null;
    }

    return new ReadStatusDto(
        status.getId(),
        status.getUser().getId(),
        status.getChannel().getId(),
        status.getLastReadAt()
    );
  }

}
