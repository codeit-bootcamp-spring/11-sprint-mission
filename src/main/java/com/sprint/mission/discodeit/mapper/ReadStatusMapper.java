package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.stereotype.Component;

@Component
public class ReadStatusMapper {

  public ReadStatusDto toDto(ReadStatus readStatus) {
    return new ReadStatusDto(
        readStatus.getId(), // id
        readStatus.getUser().getId(), // userId
        readStatus.getChannel().getId(), // channelId
        readStatus.getLastReadAt(), // lastReadAt
        readStatus.isNotificationEnabled() // notificationEnabled
    );
  }
}
