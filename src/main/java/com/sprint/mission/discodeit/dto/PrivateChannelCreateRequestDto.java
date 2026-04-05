package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Channel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PrivateChannelCreateRequestDto {
    private String name;
    private String description;
    private List<UUID> memberIds;
    private Channel.ChannelType type;
    private List<UUID> participantUserIds;
}
