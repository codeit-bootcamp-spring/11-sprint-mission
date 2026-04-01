package com.sprint.mission.discodeit.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicChannelUpdateRequest {

  private String newName;
  private String newDescription;
}
