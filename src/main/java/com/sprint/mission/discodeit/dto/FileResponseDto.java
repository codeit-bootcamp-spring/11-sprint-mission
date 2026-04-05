package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.FileMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FileResponseDto {
    private UUID id;
    private String fileName;
    private String contentType;
    private byte[] content;

    public FileResponseDto(FileMessage entity) {
        this.id = entity.getId();
        this.fileName = entity.getFileName();
        this.contentType = entity.getContentType();
        this.content = entity.getContent();
    }
}
