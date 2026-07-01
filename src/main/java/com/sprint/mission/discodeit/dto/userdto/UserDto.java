package com.sprint.mission.discodeit.dto.userdto;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.User;
import java.util.UUID;

public record UserDto(
    UUID id,
    String username,
    String email,
    User.Role role,
    BinaryContentDto profile,
    Boolean online
) {

}