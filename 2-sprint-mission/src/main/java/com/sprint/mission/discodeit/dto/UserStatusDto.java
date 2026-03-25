package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.UserStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public class UserStatusDto {

    public record CreateRequest(
        UUID userId
    ) {
        public UserStatus toEntity() {
            return UserStatus.builder()
                    .userId(this.userId)
                    .lastActiveAt(Instant.now())
                    .build();
        }
    }

    public record UpdateRequest(
        Instant LastActiveAt
    ) {}

    @Builder
    public record Response(
        UUID id,
        UUID userId,
        Instant lastActiveAt,
        boolean isOnline
    ) {
        public static Response of(UserStatus userStatus) {
            return Response.builder()
                    .id(userStatus.getId())
                    .userId(userStatus.getUserId())
                    .lastActiveAt(userStatus.getLastActiveAt())
                    .isOnline(userStatus.isOnline())
                    .build();
        }
    }
}