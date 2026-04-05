package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public class UserStatusDto {

    public record CreateRequest(
            @NotNull(message = "유저 ID는 필수 항목입니다.")
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
        Instant lastActiveAt
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