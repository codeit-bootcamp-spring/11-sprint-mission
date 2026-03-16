package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class BinaryContent extends Common{

    private String fileName;
    private byte[] data;

    public BinaryContent(String fileName, byte[] data) {
        super();
        this.fileName = fileName;
        this.data = Arrays.copyOf(data, data.length);
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
}
