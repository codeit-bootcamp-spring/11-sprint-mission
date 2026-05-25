package com.sprint.mission.discodeit.storage.s3;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class AWSS3Test {

    private static Properties loadEnv() throws IOException {
        Path envPath = Path.of(".env");

        assumeTrue(Files.exists(envPath), ".env 파일이 없어서 S3 테스트를 건너뜁니다.");

        Properties props = new Properties();

        try (InputStream inputStream = Files.newInputStream(envPath)) {
            props.load(inputStream);
        }

        assumeTrue(hasText(props.getProperty("AWS_S3_ACCESS_KEY")), "AWS_S3_ACCESS_KEY가 필요합니다.");
        assumeTrue(hasText(props.getProperty("AWS_S3_SECRET_KEY")), "AWS_S3_SECRET_KEY가 필요합니다.");
        assumeTrue(hasText(props.getProperty("AWS_S3_REGION")), "AWS_S3_REGION이 필요합니다.");
        assumeTrue(hasText(props.getProperty("AWS_S3_BUCKET")), "AWS_S3_BUCKET이 필요합니다.");

        return props;
    }

    private static S3Client buildClient(Properties props) {
        return S3Client.builder()
                .region(Region.of(props.getProperty("AWS_S3_REGION")))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                props.getProperty("AWS_S3_ACCESS_KEY"),
                                props.getProperty("AWS_S3_SECRET_KEY")
                        )
                ))
                .build();
    }

    private static S3Presigner buildPresigner(Properties props) {
        return S3Presigner.builder()
                .region(Region.of(props.getProperty("AWS_S3_REGION")))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                props.getProperty("AWS_S3_ACCESS_KEY"),
                                props.getProperty("AWS_S3_SECRET_KEY")
                        )
                ))
                .build();
    }

    @Test
    void upload() throws IOException {
        Properties props = loadEnv();
        String bucket = props.getProperty("AWS_S3_BUCKET");

        try (S3Client s3Client = buildClient(props)) {
            String key = "test/" + UUID.randomUUID();
            byte[] bytes = "hello s3 upload".getBytes(UTF_8);

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType("text/plain")
                            .build(),
                    RequestBody.fromBytes(bytes)
            );

            Long contentLength = s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            ).contentLength();

            assertThat(contentLength).isEqualTo(bytes.length);

            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
        }
    }

    @Test
    void download() throws IOException {
        Properties props = loadEnv();
        String bucket = props.getProperty("AWS_S3_BUCKET");

        try (S3Client s3Client = buildClient(props)) {
            String key = "test/" + UUID.randomUUID();
            String content = "hello s3 download";

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType("text/plain")
                            .build(),
                    RequestBody.fromString(content, UTF_8)
            );

            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );

            assertThat(response.asUtf8String()).isEqualTo(content);

            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
        }
    }

    @Test
    void presignedUrl() throws IOException {
        Properties props = loadEnv();
        String bucket = props.getProperty("AWS_S3_BUCKET");

        try (
                S3Client s3Client = buildClient(props);
                S3Presigner presigner = buildPresigner(props)
        ) {
            String key = "test/" + UUID.randomUUID();
            String content = "hello presigned url";

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType("text/plain")
                            .build(),
                    RequestBody.fromString(content, UTF_8)
            );

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

            System.out.println("Presigned URL:");
            System.out.println(presignedRequest.url());

            assertThat(presignedRequest.url().toString()).startsWith("https://");

            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}