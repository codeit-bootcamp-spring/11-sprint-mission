package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.service.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.storage.S3BinaryContentStorage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class S3BinaryContentStorageTest {

    private static S3BinaryContentStorage storage;
    private static String bucket;

    @BeforeAll
    static void setup() throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(".env")) {
            props.load(fis);
        }

        String accessKey = props.getProperty("AWS_S3_ACCESS_KEY");
        String secretKey = props.getProperty("AWS_S3_SECRET_KEY");
        String region = props.getProperty("AWS_S3_REGION");
        bucket = props.getProperty("AWS_S3_BUCKET");
        long expiration = Long.parseLong(props.getProperty("AWS_S3_PRESIGNED_URL_EXPIRATION", "600"));

        storage = new S3BinaryContentStorage(accessKey, secretKey, region, bucket, expiration);
    }

    @Test
    void put_파일을_S3에_업로드한다() {
        UUID id = UUID.randomUUID();
        byte[] content = "S3BinaryContentStorage 업로드 테스트".getBytes(StandardCharsets.UTF_8);

        UUID result = storage.put(id, content);

        assertThat(result).isEqualTo(id);
    }

    @Test
    void get_S3에서_파일을_다운로드한다() throws IOException {
        UUID id = UUID.randomUUID();
        byte[] content = "S3BinaryContentStorage 다운로드 테스트".getBytes(StandardCharsets.UTF_8);
        storage.put(id, content);

        try (InputStream inputStream = storage.get(id)) {
            byte[] downloaded = inputStream.readAllBytes();
            assertThat(downloaded).isEqualTo(content);
        }
    }

    @Test
    void download_PresignedUrl로_리다이렉트_응답을_반환한다() {
        UUID id = UUID.randomUUID();
        storage.put(id, "redirect 테스트".getBytes(StandardCharsets.UTF_8));

        BinaryContentDto dto = BinaryContentDto.builder()
                .id(id)
                .fileName("test.txt")
                .size(14L)
                .contentType("text/plain")
                .build();

        ResponseEntity<?> response = storage.download(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
        assertThat(location).isNotBlank();
        assertThat(location).contains(bucket);
        assertThat(location).contains(id.toString());
    }
}
