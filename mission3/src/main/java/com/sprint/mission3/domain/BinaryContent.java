package com.sprint.mission3.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter

public class BinaryContent {
    private final String id;
    private final String originalFileName;
    private final long size;
    private final String extension;

    public BinaryContent(String id, String originalFileName, long size) {
        this.id = UUID.randomUUID().toString();
        this.originalFileName = originalFileName;
        this.size = size;
        this.extension = extractExtension(originalFileName);
    }

}
