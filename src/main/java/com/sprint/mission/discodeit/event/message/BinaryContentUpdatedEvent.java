package com.sprint.mission.discodeit.event.message;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import lombok.Getter;

import java.time.Instant;

@Getter
public class BinaryContentUpdatedEvent {

    private final BinaryContentDto data;
    private final Instant updatedAt;

    public BinaryContentUpdatedEvent(BinaryContentDto data, Instant updatedAt) {
        this.data = data;
        this.updatedAt = updatedAt;
    }
}
