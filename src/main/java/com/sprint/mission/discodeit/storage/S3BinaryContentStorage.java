package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

  @Override
  public UUID put(UUID binaryContentId, byte[] bytes) {
    // S3 저장소는 과금 요소라 이번 구현 범위에서 제외함
    // storage.type=s3로 실행하면 명확히 미구현 예외를 던지도록 함
    throw new UnsupportedOperationException("S3 storage is not implemented.");
  }

  @Override
  public InputStream get(UUID binaryContentId) {
    // S3 저장소는 과금 요소라 이번 구현 범위에서 제외함
    throw new UnsupportedOperationException("S3 storage is not implemented.");
  }

  @Override
  public ResponseEntity<?> download(BinaryContentDto metaData) {
    // S3 저장소는 과금 요소라 이번 구현 범위에서 제외함
    throw new UnsupportedOperationException("S3 storage is not implemented.");
  }
}