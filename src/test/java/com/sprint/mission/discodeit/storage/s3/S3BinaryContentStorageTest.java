package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.storage.DownloadResult;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class S3BinaryContentStorageTest {

  private static S3BinaryContentStorage storage;
  private static UUID uploadedId;

  @BeforeAll
  static void setUp() throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream(".env"));

    storage = new S3BinaryContentStorage(
        props.getProperty("AWS_S3_ACCESS_KEY"),
        props.getProperty("AWS_S3_SECRET_KEY"),
        props.getProperty("AWS_S3_REGION"),
        props.getProperty("AWS_S3_BUCKET"),
        600L
    );
    uploadedId = UUID.randomUUID();
  }

  @Test
  @DisplayName("uploads file to S3 and returns the same UUID")
  void put() {
    // given
    byte[] content = "hello s3 storage".getBytes();

    // when
    UUID result = storage.put(uploadedId, content);

    // then
    assertThat(result).isEqualTo(uploadedId);
  }

  @Test
  @DisplayName("returns InputStream for the uploaded file by UUID")
  void get() throws IOException {
    // given
    storage.put(uploadedId, "hello s3 storage".getBytes());

    // when
    InputStream inputStream = storage.get(uploadedId);

    // then
    assertThat(inputStream).isNotNull();
    assertThat(new String(inputStream.readAllBytes())).isEqualTo("hello s3 storage");
  }

  @Test
  @DisplayName("returns Redirect result containing a presigned URL")
  void download() {
    // given
    storage.put(uploadedId, "hello s3 storage".getBytes());
    BinaryContentResponse dto = new BinaryContentResponse(
        uploadedId, "test.txt", 16L, "text/plain"
    );

    // when
    DownloadResult result = storage.download(dto);

    // then
    assertThat(result).isInstanceOf(DownloadResult.Redirect.class);
    DownloadResult.Redirect redirect = (DownloadResult.Redirect) result;
    assertThat(redirect.url()).contains(uploadedId.toString());
    System.out.println("presigned url: " + redirect.url());
  }
}