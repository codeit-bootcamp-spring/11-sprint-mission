package com.sprint.mission.discodeit.dto.user;

public record UserCreateRequest(
        String nickname,
        String username,
        String email,
        String password,
        String phoneNumber
) {}
