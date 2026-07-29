package com.sprint.mission.discodeit.event.channel;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import java.util.List;
import java.util.UUID;

public record ChannelCreatedEvent(
    ChannelDto data,
    List<UUID> participantIds
) {

  // 퍼블릭 채널용 생성자 - 퍼블릭 채널은 참여자 아이디 없음
  public ChannelCreatedEvent(ChannelDto data) {
    this(data, List.of());
  }
}
