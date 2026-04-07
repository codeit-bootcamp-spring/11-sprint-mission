package com.sprint.mission.discodeit.dto;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PrivateChannelCreateRequestDto {

  private List<UUID> participantIds;

}
