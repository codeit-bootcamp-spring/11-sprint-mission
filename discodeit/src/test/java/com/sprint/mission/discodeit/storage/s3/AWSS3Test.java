package com.sprint.mission.discodeit.storage.s3;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class AWSS3Test {

    private static S3Client s3Client;
    private static S3Presigner s3Presigner;
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

        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );

        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();

        s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Test
    void 파일_업로드() {
        String key = "test/upload-" + System.currentTimeMillis() + ".txt";
        byte[] content = "S3 업로드 테스트".getBytes(StandardCharsets.UTF_8);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build(),
                RequestBody.fromBytes(content)
        );
    }

    @Test
    void 파일_다운로드() throws IOException {
        String key = "test/download-" + System.currentTimeMillis() + ".txt";
        byte[] content = "S3 다운로드 테스트".getBytes(StandardCharsets.UTF_8);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build(),
                RequestBody.fromBytes(content)
        );

        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        )) {
            byte[] downloaded = response.readAllBytes();
            assertThat(downloaded).isEqualTo(content);
        }
    }

    @Test
    void PresignedUrl_생성() {
        String key = "test/presign-" + System.currentTimeMillis() + ".txt";

        // PresignedUrl 생성 대상 파일 업로드
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build(),
                RequestBody.fromString("PresignedUrl 테스트", StandardCharsets.UTF_8)
        );

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .build())
                        .build()
        );

        String url = presigned.url().toString();
        assertThat(url).isNotBlank();
        assertThat(url).contains(bucket);
        assertThat(url).contains(key);
    }
}
