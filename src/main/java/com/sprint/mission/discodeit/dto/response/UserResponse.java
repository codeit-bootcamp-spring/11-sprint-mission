package com.sprint.mission.discodeit.dto.response;

import lombok.Getter;

import java.util.UUID;

@Getter
public class UserResponse {
    private UUID userId;
    private String username;
    private String email;
    private boolean isOnline;
    private UUID profileId;

    public UserResponse(UUID userId, String username, String email, boolean isOnline, UUID profileId) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.isOnline = isOnline;
        this.profileId = profileId;
    }
}
