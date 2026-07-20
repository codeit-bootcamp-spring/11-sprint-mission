package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "binary_contents")
@NoArgsConstructor
public class BinaryContent extends BaseUpdatableEntity {

  // 필드
  // file_name varchar(255) not null
  // length default가 255여서 ide 경고
  @Column(name = "file_name", length = 255, nullable = false)
  private String fileName; // 파일 이름

  // size bigint not null
  @Column(name = "size", nullable = false)
  private Long size; // 파일 크기

  // content_type varchar(100) not null
  @Column(name = "content_type", length = 100, nullable = false)
  private String contentType; // 데이터 타입(.png 등)

  // status varchar(20) not null
  // 기본값은 진행중(PROCESSING)으로 설정
  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20, nullable = false)
  private BinaryContentStatus status = BinaryContentStatus.PROCESSING;

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

  // 업로드 상태를 변경 진행중 → 성공/실패로 전환
  public void updateStatus(BinaryContentStatus status) {
    this.status = status;
  }

  // 데이터 업로드 상태(진행중, 성공, 실패)
  public enum BinaryContentStatus {
    PROCESSING, SUCCESS, FAIL;
  }
}
