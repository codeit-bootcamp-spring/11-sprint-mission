package com.sprint.mission.discodeit.storage.s3;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

class AWSS3Test {

  private S3Client s3Client;
  private S3Presigner presigner;
  private String bucket;
  private String region;

  @BeforeEach
  void setUp() throws IOException {
    Properties props = new Properties();
    try (InputStream is = new FileInputStream(".env")) {
      props.load(is);
    }

    String accessKey = props.getProperty("AWS_S3_ACCESS_KEY");
    String secretKey = props.getProperty("AWS_S3_SECRET_KEY");
    region = props.getProperty("AWS_S3_REGION");
    bucket = props.getProperty("AWS_S3_BUCKET");

    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

    s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(credentialsProvider)
        .build();

    presigner = S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(credentialsProvider)
        .build();
  }

  @Test
  void upload() throws IOException {
    String key = "test/hello.txt";
    byte[] content = "Hello, S3!".getBytes();

    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build(),
        RequestBody.fromBytes(content)
    );

    System.out.println("업로드 완료: " + key);
  }

  @Test
  void download() throws IOException {
    String key = "test/hello.txt";
    Path tempFile = Path.of(System.getProperty("java.io.tmpdir"), "s3-download-test.txt");
    Files.deleteIfExists(tempFile);

    s3Client.getObject(
        GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build(),
        tempFile
    );

    String content = Files.readString(tempFile);
    System.out.println("다운로드 내용: " + content);
    assertNotNull(content);
  }

  @Test
  void generatePresignedUrl() {
    String key = "test/hello.txt";

    String url = presigner.presignGetObject(
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .getObjectRequest(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build())
            .build()
    ).url().toString();

    System.out.println("PresignedUrl: " + url);
    assertNotNull(url);
  }
}
