package com.sprint.mission.discodeit.dto;

public record UserUpdateRequest(
        String nickname,
        String username,
        String email,
        String password,
        String phoneNumber
) {}
