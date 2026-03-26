package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record UserUpdateRequest (
        UUID id,
        String userName,
        String email,
        String password,
        String statusMessage,
        BinaryContentCreateRequest profileImage
){
}
