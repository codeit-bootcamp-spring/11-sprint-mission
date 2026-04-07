package com.sprint.mission.discodeit.dto;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class ChannelCreateRequestDto {

  private String name;
  private String description;
  private List<UUID> participantIds;    // 프론트가 보내는 필드명
  private List<UUID> participantUserIds; // 서비스에서 사용하는 필드명

  // participantIds -> participantUserIds 통일을 위해서
  public List<UUID> getParticipantUserIds() {
    return participantUserIds != null ? participantUserIds : participantIds;
  }

}
