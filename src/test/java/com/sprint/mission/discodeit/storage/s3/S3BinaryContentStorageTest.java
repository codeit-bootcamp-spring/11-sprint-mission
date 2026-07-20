package com.sprint.mission.discodeit.storage.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent.BinaryContentStatus;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class S3BinaryContentStorageTest {

  private S3BinaryContentStorage storage;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @BeforeEach
  void setup() throws Exception {
    Properties properties = new Properties();
    properties.load(new FileInputStream(".env"));

    String accessKey = properties.getProperty("AWS_S3_ACCESS_KEY");
    String secretKey = properties.getProperty("AWS_S3_SECRET_KEY");
    String region = properties.getProperty("AWS_S3_REGION");
    String bucket = properties.getProperty("AWS_S3_BUCKET");

    storage = new S3BinaryContentStorage(accessKey, secretKey, region, bucket, 600, eventPublisher);

  }

  @Test
  @DisplayName("S3 업로드 테스트")
  void S3_업로드_테스트() {
    // given
    UUID id = UUID.randomUUID();
    byte[] bytes = "test".getBytes(StandardCharsets.UTF_8);

    // when
    UUID result = storage.put(id, bytes);

    // then
    assertEquals(id, result);
  }

  @Test
  @DisplayName("S3 다운로드 테스트")
  void S3_다운로드_테스트() throws Exception {
    // given
    UUID id = UUID.randomUUID();
    byte[] bytes = "test".getBytes(StandardCharsets.UTF_8);

    storage.put(id, bytes);

    // when
    InputStream inputStream = storage.get(id);
    Path target = Path.of("build/downloadText.txt");
    Files.deleteIfExists(target); // Files.write 실행 전 이미 파일이 있을 경우 삭제
    Files.write(target, inputStream.readAllBytes());

    // then
    assertTrue(target.toFile().exists());

    String content = Files.readString(target);
    assertEquals("test", content);
  }

  @Test
  @DisplayName("Presigned URL 테스트")
  void Presigned_URL_리다이렉트_테스트() {
    // given
    UUID id = UUID.randomUUID();

    BinaryContentDto dto = new BinaryContentDto(id, "test.txt", 100L, "text/plain",
        BinaryContentStatus.SUCCESS);

    storage.put(id, "Presigned URL Test".getBytes(StandardCharsets.UTF_8));

    // when
    ResponseEntity<Void> response = storage.download(dto);

    // then
    assertEquals(HttpStatus.FOUND, response.getStatusCode());

    String url = response.getHeaders().getLocation().toString();
    System.out.println("Presigned URL : " + url);
    assertNotNull(url);

  }

  @AfterEach
  void tearDown() throws Exception {
    Files.deleteIfExists(Path.of("build/downloadText.txt"));
  }
}
