package com.sprint.mission.discodeit.storage;

import static org.junit.jupiter.api.Assertions.*;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Disabled
public class S3BinaryContentStorageTest {

  private static S3BinaryContentStorage storage;

  @BeforeAll
  static void setup() throws IOException {
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream(".env")) {
      props.load(fis);
    }

    storage = new S3BinaryContentStorage(
        props.getProperty("AWS_S3_ACCESS_KEY"),
        props.getProperty("AWS_S3_SECRET_KEY"),
        props.getProperty("AWS_S3_REGION"),
        props.getProperty("AWS_S3_BUCKET"),
        600L
    );
  }

  @Test
  void putAndGetAndDeleteTest() throws IOException {
    UUID id = UUID.randomUUID();
    String text = "S3 Storage 동작 테스트";
    byte[] testData = text.getBytes();

    UUID saveId = storage.put(id, testData);
    assertEquals(id, saveId, "업로드 시 반환된 ID가 일치해야 함");

    try (InputStream is = storage.get(saveId)) {
      String downloadedContent = new String(is.readAllBytes());
      assertEquals(text, downloadedContent, "다운로드한 내용이 업로드한 내용이 일치해야 함");
    }

    storage.delete(saveId);

    System.out.println("S3 파일 업로드 -> 다운로드 -> 삭제 테스트 성공");
  }

  @Test
  void downloadPresignedUrlTest() {
    BinaryContentDto mockDto = Mockito.mock(BinaryContentDto.class);
    Mockito.when(mockDto.id()).thenReturn(UUID.randomUUID());

    ResponseEntity<?> response = storage.download(mockDto);

    assertEquals(HttpStatus.FOUND, response.getStatusCode());

    assertNotNull(response.getHeaders().getLocation());
    assertTrue(response.getHeaders().getLocation().toString().contains("amazonaws.com"));

    System.out.println("Presigned URL 리다이렉트 응답 생성 성공");
    System.out.println("발급된 URL: " + response.getHeaders().getLocation().toString());
  }
}
