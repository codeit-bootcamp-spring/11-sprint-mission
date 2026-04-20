package com.sprint.mission.discodeit.dto.data;

import java.util.UUID;

public record UserDto(
    UUID id,
    String username,
    String email,
    BinaryContentDto profile, // UUID 대신 Dto 객체 참조
    Boolean online
) {

}