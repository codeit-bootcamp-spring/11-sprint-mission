package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "binary_contents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BinaryContent extends BaseEntity {

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private String contentType;

    public BinaryContent(byte[] bytes, String fileName, String contentType) {
        super();
        this.size = bytes == null ? 0 : bytes.length;
        this.fileName = fileName == null ? "" : fileName;
        this.contentType = contentType == null ? "" : contentType;
    }


}
