package com.sprint.mission.discodeit.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class MessageCreateMultipartDoc {
    public MessageCreatePart messageCreateRequest;

    @ArraySchema(schema = @Schema(type = "string", format = "binary"))
    public List<String> attachments;
}