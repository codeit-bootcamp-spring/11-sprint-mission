package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
public class S3BinaryContentStorageTest {

  @Mock
  private S3Client s3Client;

  @Mock
  private S3Presigner s3Presigner;

  private S3BinaryContentStorage storage;

  private String testBucket = "test-bucket";

  @BeforeEach
  void setup() {
    ApplicationEventPublisher mockEventPublisher = Mockito.mock(ApplicationEventPublisher.class);

    storage = new S3BinaryContentStorage(
        "dummy-access-key",
        "dummy-secret-key",
        "ap-northeast-2",
        testBucket,
        10L,
        mockEventPublisher
    );

    // 현재 S3BinaryContentStorage 내부에서 S3Client와 S3Presigner를 받고 있으므로, 테스트를 위해 ReflectionTestUtils를 사용해 내부의 진짜 객체를 가짜(Mock) 객체로 덮어씌운다.
    ReflectionTestUtils.setField(storage, "s3Client", s3Client);
    ReflectionTestUtils.setField(storage, "s3Presigner", s3Presigner);
  }

  @Test
  @DisplayName("바이너리 데이터를 업로드하면 S3Client의 putObject가 호출되고 UUID를 반환한다.")
  void putTest() {
    //given
    UUID id = UUID.randomUUID();
    byte[] bytes = "S3 Upload Test".getBytes();

    given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .willReturn(PutObjectResponse.builder().build());

    //when
    UUID result = storage.put(id, bytes);

    //then
    assertThat(result).isEqualTo(id);
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @DisplayName("S3에 저장된 파일을 UUID로 조회하면, 정상적인 InputStream을 반환해야 한다.")
  void getTest() throws IOException {
    //given
    UUID id = UUID.randomUUID();
    String testString = "S3 Get Test";

    InputStream byteArrayInputStream = new ByteArrayInputStream(testString.getBytes());

    ResponseInputStream<GetObjectResponse> fakeStream = new ResponseInputStream<>(
        GetObjectResponse.builder().build(), byteArrayInputStream);

    given(s3Client.getObject(any(GetObjectRequest.class)))
        .willReturn(fakeStream);

    //when (try-with-resource 구문 사용)
    try (InputStream result = storage.get(id)) {
      //then
      assertThat(result).isNotNull();
      assertThat(new String(result.readAllBytes())).isEqualTo(testString);
    }
    verify(s3Client).getObject(any(GetObjectRequest.class));
  }

  @Test
  @DisplayName("파일 다운로드를 요청하면, 302 FOUND 상태 코드와 Presigned URL이 담긴 응답을 반환해야 한다.")
  void downloadTest() throws Exception {
    //given
    UUID id = UUID.randomUUID();
    BinaryContentDto binaryContentDto = new BinaryContentDto(id, "testFile", 1L, ".txt");

    URL fakeUrl = new URI("https://fake-s3-url.com").toURL();
    PresignedGetObjectRequest fakePresignedRequest = Mockito.mock(PresignedGetObjectRequest.class);

    given(fakePresignedRequest.url())
        .willReturn(fakeUrl);
    given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
        .willReturn(fakePresignedRequest);

    //when
    ResponseEntity<?> result = storage.download(binaryContentDto);

    //then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    assertThat(result.getHeaders().getLocation()).isEqualTo(fakeUrl.toURI());
  }
}
