package com.sprint.mission.discodeit.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MessageCreateRequest {

    @NotBlank
    private String content;

    @NotBlank
    private UUID authorId;

    @NotBlank
    private UUID channelId;

    private UUID receiverId;
    private String fileName;
    private byte[] fileContent;
    private String contentType;
}
