package com.sprint.mission.discodeit.dto.messagedto;


import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public record UpdateMessageDto(


        UUID messageId,
        String content,
        List<MultipartFile> binaryFile

) {




}
