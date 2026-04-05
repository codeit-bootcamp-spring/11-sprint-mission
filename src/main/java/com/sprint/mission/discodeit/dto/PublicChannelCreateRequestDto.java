package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Channel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class PublicChannelCreateRequestDto {
    private String name;
    private String description;
    private Channel.ChannelType type;
}
