package com.sprint.mission.discodeit.dto;

public record UserResponse(
        String nickname,
        String username,
        String email,
        String phoneNumber,
        BinaryContentResponse profile,
        UserStatusResponse status
) {}
