package com.sprint.mission.discodeit.dto.userdto;

import com.sprint.mission.discodeit.binary.BinaryFile;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.UUID;

public record UserInfoDto(UUID userId,
                          String nickname,
                          String email,
                          BinaryFile binaryFile,
                          UserStatus status


) {
}
