package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserStatusResponseDto {
    private UUID id;
    private UUID userId;
    private Instant lastAccessAt;
    private boolean online;

    public UserStatusResponseDto(UserStatus status) {
        this.id = status.getId();
        this.userId = status.getUserId();
        this.lastAccessAt = status.getLastAccessAt();
        this.online = status.isOnline();
    }
}
