package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import com.sprint.mission.discodeit.entity.base.ImmutableBaseEntity;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class BinaryContent extends ImmutableBaseEntity {

    private String fileName;
    private String contentType;
    private byte[] data;

    private BinaryContent(String fileName, String contentType, byte[] data) {
        super();
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data.clone();
    }

    private BinaryContent(BinaryContent other) {
        super(other);
        this.fileName = other.fileName;
        this.contentType = other.contentType;
        this.data = other.data.clone();
    }

    @Override
    public BinaryContent copy() {
        return new BinaryContent(this);
    }

    public static BinaryContent create(String fileName, String contentType, byte[] data) {
        return new BinaryContent(fileName, contentType, data);
    }
}
