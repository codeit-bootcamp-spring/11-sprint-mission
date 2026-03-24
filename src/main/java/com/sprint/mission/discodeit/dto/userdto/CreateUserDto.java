package com.sprint.mission.discodeit.dto.userdto;

import com.sprint.mission.discodeit.binary.BinaryFile;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.UUID;

public record CreateUserDto(String nickname,
                            String email,
                            String password,
                            BinaryFile binaryFile
) {}

