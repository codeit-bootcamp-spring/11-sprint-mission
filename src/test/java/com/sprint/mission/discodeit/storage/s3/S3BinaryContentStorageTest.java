package com.sprint.mission.discodeit.storage.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

public class S3BinaryContentStorageTest {

    private S3BinaryContentStorage storage;

    @BeforeEach
    void setUp() throws IOException {
        // .env 파일에서 AWS 설정값 로드
        Properties props = new Properties();
        props.load(new FileInputStream(
                "C:/Users/dkskr/Downloads/11-sprint-mission/.env"
        ));

        // S3BinaryContentStorage 생성
        storage = new S3BinaryContentStorage(
                props.getProperty("AWS_S3_ACCESS_KEY"),
                props.getProperty("AWS_S3_SECRET_KEY"),
                props.getProperty("AWS_S3_REGION"),
                props.getProperty("AWS_S3_BUCKET"),
                600L
        );
    }

    @Test
    void put() throws IOException {
        UUID id = UUID.randomUUID();
        byte[] content = "Hello S3BinaryContentStorage!".getBytes();

        UUID result = storage.put(id, content);
        System.out.println("put 성공 id: " + result);
    }

    @Test
    void get() throws IOException {
        // 먼저 업로드
        UUID id = UUID.randomUUID();
        storage.put(id, "Hello S3!".getBytes());

        // 다운로드
        InputStream inputStream = storage.get(id);
        System.out.println("get 성공: " + new String(inputStream.readAllBytes()));
    }

    @Test
    void download() {
        // 먼저 업로드
        UUID id = UUID.randomUUID();
        storage.put(id, "Hello S3!".getBytes());

        // BinaryContentDto 만들어서 넘기기
        BinaryContentDto dto = new BinaryContentDto(
                id,
                "hello.txt",
                (long) "Hello S3!".getBytes().length,
                "text/plain"
        );

        // PresignedUrl 생성
        var response = storage.download(dto);
        System.out.println("download 상태코드: " + response.getStatusCode());
        System.out.println("download URL: " + response.getHeaders().getLocation());
    }
}