package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

class S3BinaryContentStorageTest {

    private S3BinaryContentStorage storage;

    @BeforeEach
    void setUp() {
        storage = new S3BinaryContentStorage(
            "test-access-key", "test-secret-key", "ap-northeast-2", "test-bucket", 600L
        );
    }

    @Test
    @DisplayName("put - 바이트 배열 업로드 후 동일한 UUID 반환")
    void put_업로드_후_동일한_UUID_반환() {
        UUID id = UUID.randomUUID();
        byte[] data = "hello s3".getBytes();
        S3Client mockS3Client = mock(S3Client.class);
        S3ClientBuilder mockBuilder = mock(S3ClientBuilder.class, RETURNS_SELF);

        try (MockedStatic<S3Client> mockedS3 = mockStatic(S3Client.class)) {
            mockedS3.when(S3Client::builder).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockS3Client);
            when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

            UUID result = storage.put(id, data);

            assertThat(result).isEqualTo(id);
            verify(mockS3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }
    }

    @Test
    @DisplayName("get - UUID로 S3 객체 다운로드 후 InputStream 반환")
    void get_S3_객체_다운로드_후_InputStream_반환() throws IOException {
        UUID id = UUID.randomUUID();
        byte[] expected = "downloaded content".getBytes();
        S3Client mockS3Client = mock(S3Client.class);
        S3ClientBuilder mockBuilder = mock(S3ClientBuilder.class, RETURNS_SELF);

        try (MockedStatic<S3Client> mockedS3 = mockStatic(S3Client.class)) {
            mockedS3.when(S3Client::builder).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockS3Client);
            ResponseBytes<GetObjectResponse> responseBytes =
                ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), expected);
            when(mockS3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);

            var result = storage.get(id);

            assertThat(result.readAllBytes()).isEqualTo(expected);
        }
    }

    @Test
    @DisplayName("download - PresignedUrl 기반 302 리다이렉트 응답 반환")
    void download_PresignedUrl_302_리다이렉트_반환() throws MalformedURLException {
        UUID id = UUID.randomUUID();
        BinaryContentDto dto = new BinaryContentDto(id, "file.jpg", 1024L, "image/jpeg");
        String expectedUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/" + id + "?signature=abc";

        S3Presigner mockPresigner = mock(S3Presigner.class);
        S3Presigner.Builder mockPresignerBuilder = mock(S3Presigner.Builder.class, RETURNS_SELF);
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);

        try (MockedStatic<S3Presigner> mockedPresigner = mockStatic(S3Presigner.class)) {
            mockedPresigner.when(S3Presigner::builder).thenReturn(mockPresignerBuilder);
            when(mockPresignerBuilder.build()).thenReturn(mockPresigner);
            when(presignedRequest.url()).thenReturn(new URL(expectedUrl));
            when(mockPresigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

            ResponseEntity<?> response = storage.download(dto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
            assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create(expectedUrl));
        }
    }
}