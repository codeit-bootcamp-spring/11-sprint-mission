package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.ReadStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ReadStatusResponseDto {
    private UUID id;
    private UUID userId;
    private UUID channelId;
    private Instant lastReadAt;
    private boolean isRead;

    public ReadStatusResponseDto(ReadStatus entity) {
        this.id = entity.getId();
        this.userId = entity.getUserId();
        this.channelId = entity.getChannelId();
        this.lastReadAt = entity.getLastReadAt();
        this.isRead = entity.isRead;
    }
}
