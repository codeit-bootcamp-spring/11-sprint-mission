package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class BinaryContent extends BaseEntity {

    private String fileName;
    private long size;
    private String contentType;

    public BinaryContent(String fileName, long size, String contentType) {
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
    }
}