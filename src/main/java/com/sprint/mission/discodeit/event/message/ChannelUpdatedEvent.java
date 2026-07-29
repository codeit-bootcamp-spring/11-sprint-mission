package com.sprint.mission.discodeit.event.message;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ChannelUpdatedEvent {

    private final ChannelDto data;
    private final Instant updatedAt;

    public ChannelUpdatedEvent(ChannelDto data, Instant updatedAt) {
        this.data = data;
        this.updatedAt = updatedAt;
    }
}
