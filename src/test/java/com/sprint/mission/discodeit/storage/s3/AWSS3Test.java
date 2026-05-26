package com.sprint.mission.discodeit.storage.s3;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
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

@Disabled
public class AWSS3Test {

  private static Properties loadEnv() throws IOException {
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream(".env")) {
      props.load(fis);
    }
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
        )).build();
  }

  @Test
  void upload() throws IOException {
    Properties props = loadEnv();
    S3Client s3Client = buildClient(props);
    String bucket = props.getProperty("AWS_S3_BUCKET");

    String key = UUID.randomUUID().toString() + ".txt";
    String content = "AWS S3 업로드 테스트";

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket).key(key).build();

    s3Client.putObject(putObjectRequest, RequestBody.fromString(content));
    System.out.println("업로드 성공, 파일명: " + key);
  }

  @Test
  void download() throws IOException {
    Properties props = loadEnv();
    S3Client s3Client = buildClient(props);
    String bucket = props.getProperty("AWS_S3_BUCKET");

    String key = "download-test-" + UUID.randomUUID().toString() + ".txt";
    s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(),
        RequestBody.fromString("다운로드 성공"));

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket).key(key).build();

    try (ResponseInputStream<GetObjectResponse> inputStream =
        s3Client.getObject(getObjectRequest)) {
      String downloadedContent = new String(inputStream.readAllBytes());
      System.out.println("다운로드 성공: " + downloadedContent);
    }
  }

  @Test
  void presignedUrl() throws IOException {
    Properties props = loadEnv();
    String bucket = props.getProperty("AWS_S3_BUCKET");

    String key = "presigned-test-" + UUID.randomUUID().toString() + ".txt";
    S3Client s3Client = buildClient(props);
    s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(),
        RequestBody.fromString("브라우저에서 텍슽트 보이면 성공"));

    try (S3Presigner presigner = S3Presigner.builder()
        .region(Region.of(props.getProperty("AWS_S3_REGION")))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(
                props.getProperty("AWS_S3_ACCESS_KEY"),
                props.getProperty("AWS_S3_SECRET_KEY")
            )
        )).build()) {
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(bucket).key(key).build();

      GetObjectPresignRequest presignRequest = GetObjectPresignRequest
          .builder().signatureDuration(Duration.ofMinutes(10))
          .getObjectRequest(getObjectRequest).build();

      PresignedGetObjectRequest presignedRequest =
          presigner.presignGetObject(presignRequest);
      String url = presignedRequest.url().toString();

      System.out.println("Presigned URL 발급 성공");
      System.out.println(url);
    }
  }
}