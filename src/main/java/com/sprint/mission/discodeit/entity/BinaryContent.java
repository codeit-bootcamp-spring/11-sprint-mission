package com.sprint.mission.discodeit.entity;

import lombok.Getter;

@Getter
public class BinaryContent extends BaseEntity {
    private String filename;
    private Long size;
    private String contentType;
    private byte[] bytes;

    public BinaryContent(String filename, Long size, String contentType, byte[] bytes) {
        super();
        this.filename = filename;
        this.size = size;
        this.contentType = contentType;
        this.bytes = bytes;
    }

    // setUpdateAt 호출하지 않음
}
