package com.sprint.mission.discodeit.storage.s3;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.net.URL;

public class AWSS3Test {

  private S3Client s3Client;
  private S3Presigner s3Presigner;
  private String bucket;
  private String region;

  @BeforeEach
  void setUp() throws IOException {
    // .env 파일 로드
    Properties props = new Properties();
    props.load(new FileInputStream(".env"));

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

    s3Presigner = S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(credentialsProvider)
        .build();
  }

  @Test
  void upload() {
    String key = "test/test-file.txt";
    byte[] content = "Hello S3!".getBytes();

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
    String key = "test/test-file.txt";

    InputStream inputStream = s3Client.getObject(
        GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()
    );

    String content = new String(inputStream.readAllBytes());
    System.out.println("다운로드 완료: " + content);
  }

  @Test
  void PresignedUrl_생성() {
    String key = "test/test-file.txt";

    URL presignedUrl = s3Presigner.presignGetObject(
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .getObjectRequest(
                GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()
            )
            .build()
    ).url();

    System.out.println("PresignedUrl: " + presignedUrl);
  }
}
