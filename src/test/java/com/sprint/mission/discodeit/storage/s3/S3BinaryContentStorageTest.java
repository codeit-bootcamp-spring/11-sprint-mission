package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class S3BinaryContentStorageTest {

  private S3BinaryContentStorage storage;

  @BeforeEach
  void setUp() throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream(".env"));

    String accessKey = props.getProperty("AWS_S3_ACCESS_KEY");
    String secretKey = props.getProperty("AWS_S3_SECRET_KEY");
    String region = props.getProperty("AWS_S3_REGION");
    String bucket = props.getProperty("AWS_S3_BUCKET");

    storage = new S3BinaryContentStorage(accessKey, secretKey, region, bucket, 600);
  }

  @Test
  void put() {
    UUID id = UUID.randomUUID();
    byte[] content = "test content".getBytes();

    UUID result = storage.put(id, content);

    assertThat(result).isEqualTo(id);
  }

  @Test
  void get() throws IOException {
    UUID id = UUID.randomUUID();
    byte[] content = "test content".getBytes();
    storage.put(id, content);

    InputStream result = storage.get(id);

    assertThat(new String(result.readAllBytes())).isEqualTo("test content");
  }

  @Test
  void download() {
    UUID id = UUID.randomUUID();
    byte[] content = "test content".getBytes();
    storage.put(id, content);

    BinaryContentDto dto = new BinaryContentDto(id, "test.txt", (long) content.length, "text/plain");
    ResponseEntity<?> response = storage.download(dto);

    assertThat(response.getStatusCode().value()).isEqualTo(302);
    assertThat(response.getHeaders().getLocation()).isNotNull();
  }
}