package com.sprint.mission.discodeit.event.channel;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.event.DeletedEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ChannelDeletedEvent extends DeletedEvent<ChannelDto> {

  // 삭제할 채널 참가자들(공개채널이어도 List로 다 가져와야함)
  private final List<UUID> receiverIds;

  public ChannelDeletedEvent(ChannelDto data, Instant deletedAt, List<UUID> receiverIds) {
    super(data, deletedAt);
    this.receiverIds = receiverIds;
  }

}
