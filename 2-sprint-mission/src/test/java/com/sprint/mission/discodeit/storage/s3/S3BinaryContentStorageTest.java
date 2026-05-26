package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("S3BinaryContentStorage 테스트")
class S3BinaryContentStorageTest {

  @Autowired
  private S3BinaryContentStorage s3BinaryContentStorage;

  private final List<UUID> uploadedIds = new ArrayList<>();

  private final byte[] testData =
      "S3 Test Data".getBytes(StandardCharsets.UTF_8);

  @Test
  @DisplayName("S3 스토리지 빈이 정상적으로 등록된다")
  void s3_storage_bean_loading_test() {
    // then
    assertThat(s3BinaryContentStorage)
        .isNotNull()
        .isInstanceOf(S3BinaryContentStorage.class);
  }

  @Test
  @DisplayName("S3에 파일 업로드 성공")
  void put_success() throws Exception {
    // given
    UUID testId = UUID.randomUUID();

    // when
    UUID savedId = s3BinaryContentStorage.put(testId, testData);
    uploadedIds.add(savedId);

    // then
    assertThat(savedId).isEqualTo(testId);

    try (InputStream inputStream = s3BinaryContentStorage.get(savedId)) {
      byte[] downloadedBytes = inputStream.readAllBytes();
      assertThat(downloadedBytes).isEqualTo(testData);
    }
  }

  @Test
  @DisplayName("S3 파일 조회 성공")
  void get_success() throws Exception {
    // given
    UUID testId = UUID.randomUUID();
    s3BinaryContentStorage.put(testId, testData);
    uploadedIds.add(testId);

    // when & then
    try (InputStream inputStream = s3BinaryContentStorage.get(testId)) {
      assertThat(inputStream).isNotNull();
      byte[] downloadedBytes = inputStream.readAllBytes();
      assertThat(downloadedBytes).isEqualTo(testData);
    }
  }

  @Test
  @DisplayName("존재하지 않는 파일 조회 시 예외 발생")
  void get_not_found() {
    // given
    UUID invalidId = UUID.randomUUID();

    // when & then
    assertThatThrownBy(() -> s3BinaryContentStorage.get(invalidId))
        .isInstanceOf(BinaryContentNotFoundException.class);
  }

  @Test
  @DisplayName("Presigned URL 다운로드 응답 반환")
  void download_success() {
    // given
    UUID testId = UUID.randomUUID();
    s3BinaryContentStorage.put(testId, testData);
    uploadedIds.add(testId);

    BinaryContentDto.Response responseDto =
        BinaryContentDto.Response.builder()
            .id(testId)
            .contentType("text/plain")
            .fileName("test-file.txt")
            .build();

    // when
    ResponseEntity<Void> response =
        s3BinaryContentStorage.download(responseDto);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);

    String location =
        response.getHeaders().getFirst(HttpHeaders.LOCATION);

    assertThat(location)
        .isNotNull()
        .contains(testId.toString());
  }

  @AfterEach
  void tearDown() {
    for (UUID uploadedId : uploadedIds) {
      try {
        s3BinaryContentStorage.delete(uploadedId);
        log.info("테스트 파일 삭제 완료. id={}", uploadedId);
      } catch (Exception e) {
        log.warn("테스트 파일 삭제 실패. id={}", uploadedId, e);
      }
    }
    uploadedIds.clear();
  }
}