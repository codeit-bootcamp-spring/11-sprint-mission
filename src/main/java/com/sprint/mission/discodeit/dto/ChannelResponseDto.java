package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChannelResponseDto {

  private UUID id;
  private String name;
  private String description;
  private String type;        // public or private
  private List<UUID> participantIds;  // participantUserIds -> participantIds
  private Instant lastMessageAt;     // latesMessageCreatedAt-> lastMessageAt

}
