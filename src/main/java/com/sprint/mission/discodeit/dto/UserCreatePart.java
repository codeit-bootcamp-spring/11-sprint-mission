package com.sprint.mission.discodeit.dto;

public record UserCreatePart(
        String username,
        String email,
        String password
) {
}
