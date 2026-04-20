package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "binary_contents")
@NoArgsConstructor
public class BinaryContent extends BaseEntity {

  // 필드
  // file_name varchar(255) not null
  @Column(name = "file_name", length = 255, nullable = false)
  private String fileName; // 파일 이름

  // size bigint not null
  @Column(name = "size", nullable = false)
  private Long size; // 파일 크기

  // content_type varchar(100) not null
  @Column(name = "content_type", length = 100, nullable = false)
  private String contentType; // 데이터 타입(.png 등)

  // 생성자
  private BinaryContent(String fileName, Long size, String contentType) {
    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;
  }

  //  // 프로필 이미지(정적 팩토리 메서드)
  public static BinaryContent of(String fileName, Long size, String contentType) {
    return new BinaryContent(fileName, size, contentType);
  }
}
