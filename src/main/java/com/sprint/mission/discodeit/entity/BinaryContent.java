package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.MutableBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "binary_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BinaryContent extends MutableBaseEntity {

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false)
    private long size;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BinaryContentStatus status = BinaryContentStatus.PROCESSING;

    public void updateStatus(BinaryContentStatus status) {
        this.status = status;
    }

    public BinaryContent(String fileName, long size, String contentType) {
        this.id = UUID.randomUUID();
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
    }

    public enum BinaryContentStatus {
        PROCESSING,
        SUCCESS,
        FAIL
    }
}