package com.sprint.mission.discodeit.dto.userdto;

import com.sprint.mission.discodeit.binary.BinaryFile;
import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentInfoDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.UserStatus;
import lombok.Getter;

import java.util.UUID;


public record UpdateUserDto(
        UUID userId,
        String newNickname,
        String newEmail,
        String oldPassword,
        String newPassword,
        BinaryFile newProfileImg


        ) {
}
