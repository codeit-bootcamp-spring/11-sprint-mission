package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "binary_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BinaryContent extends BaseEntity {

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false)
    private long size;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    public BinaryContent(String fileName, long size, String contentType) {
        this.id = UUID.randomUUID();
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
    }
}