package com.sprint.mission.discodeit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserResponse {
    private UUID userId;
    private String username;
    private String email;
    private boolean isOnline;
    private UUID profileId;
}
