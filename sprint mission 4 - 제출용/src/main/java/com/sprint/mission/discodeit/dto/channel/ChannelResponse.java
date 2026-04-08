package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.ChannelType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChannelResponse {

  private UUID id;
  private Instant createdAt;
  private Instant updatedAt;
  private ChannelType type;
  private String name;
  private String description;
  private List<UUID> participantIds;
  private Instant lastMessageAt;
}
