package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.storage.S3BinaryContentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class S3BinaryContentStorageTest {

    private S3BinaryContentStorage storage;

    @BeforeEach
    void setUp() throws IOException {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(".env")) {
            props.load(is);
        }

        storage = new S3BinaryContentStorage(
                props.getProperty("AWS_S3_ACCESS_KEY"),
                props.getProperty("AWS_S3_SECRET_KEY"),
                props.getProperty("AWS_S3_REGION"),
                props.getProperty("AWS_S3_BUCKET"),
                600L,
                event -> { }
        );
    }

    @Test
    void put() {
        UUID id = UUID.randomUUID();
        byte[] content = "S3BinaryContentStorage test".getBytes();

        UUID result = storage.put(id, content);

        assertEquals(id, result);
        System.out.println("업로드 완료: " + result);
    }

    @Test
    void get() throws IOException {
        UUID id = UUID.randomUUID();
        byte[] content = "S3BinaryContentStorage get test".getBytes();
        storage.put(id, content);

        InputStream is = storage.get(id);

        assertNotNull(is);
        String result = new String(is.readAllBytes());
        assertEquals("S3BinaryContentStorage get test", result);
        System.out.println("다운로드 내용: " + result);
    }

    @Test
    void download() {
        UUID id = UUID.randomUUID();
        byte[] content = "S3BinaryContentStorage download test".getBytes();
        storage.put(id, content);

        BinaryContentDto dto = new BinaryContentDto(id, "test.txt", (long) content.length, "text/plain");
        ResponseEntity<?> response = storage.download(dto);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertNotNull(response.getHeaders().getLocation());
        System.out.println("PresignedUrl: " + response.getHeaders().getLocation());
    }
}
