package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

class AWSS3Test {

  private static S3Client s3Client;
  private static S3Presigner presigner;
  private static String bucket;

  @BeforeAll
  static void setup() throws IOException {
    Properties props = new Properties();
    try (FileReader reader = new FileReader(".env")) {
      props.load(reader);
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

    presigner = S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(credentialsProvider)
        .build();
  }

  @Test
  void upload() {
    UUID key = UUID.randomUUID();
    byte[] content = "Hello, S3!".getBytes();

    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key.toString())
            .build(),
        RequestBody.fromBytes(content)
    );

    var response = s3Client.headObject(
        HeadObjectRequest.builder()
            .bucket(bucket)
            .key(key.toString())
            .build()
    );
    assertThat(response.contentLength()).isEqualTo(content.length);
  }

  @Test
  void download() throws IOException {
    UUID key = UUID.randomUUID();
    byte[] content = "Download test content".getBytes();

    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key.toString())
            .build(),
        RequestBody.fromBytes(content)
    );

    byte[] downloaded = s3Client.getObject(
        GetObjectRequest.builder()
            .bucket(bucket)
            .key(key.toString())
            .build()
    ).readAllBytes();

    assertThat(downloaded).isEqualTo(content);
  }

  @Test
  void generatePresignedUrl() {
    UUID key = UUID.randomUUID();

    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key.toString())
            .build(),
        RequestBody.fromBytes("Presign test content".getBytes())
    );

    PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .getObjectRequest(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key.toString())
                .build())
            .build()
    );

    assertThat(presignedRequest.url()).isNotNull();
    assertThat(presignedRequest.url().toString()).contains(bucket);
    assertThat(presignedRequest.isBrowserExecutable()).isTrue();
  }
}
