package com.sprint.mission.discodeit.dto.userdto;


import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record CreateUserDto(String nickname,
                            String email,
                            String password,
                            MultipartFile binaryFile
) {}

