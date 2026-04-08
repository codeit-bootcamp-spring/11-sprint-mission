package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.baseentity.BaseEntity;
import lombok.Getter;

import java.util.Arrays;

@Getter
public class BinaryContent extends BaseEntity {

    private final String fileName;
    private final String contentType;
    private final byte[] data;

    public BinaryContent(String fileName, String contentType, byte[] data) {
        super();
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = Arrays.copyOf(data, data.length);
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
}
