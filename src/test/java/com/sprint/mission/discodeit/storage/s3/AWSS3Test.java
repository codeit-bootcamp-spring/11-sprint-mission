package com.sprint.mission.discodeit.storage.s3;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
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

@Disabled
public class AWSS3Test {

  // .env 파일에서 AWS 설정값을 읽어오는 유틸 메서드
  private static Properties loadEnv() throws IOException {
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream(".env")) { // 파일 읽기
      props.load(fis);
    }
    return props;
  }

  // Properties를 받아 S3Client를 생성
  private static S3Client buildClient(Properties props) {
    // Credentials 생성
    AwsBasicCredentials credentials = AwsBasicCredentials.create(
        props.getProperty("AWS_S3_ACCESS_KEY"),
        props.getProperty("AWS_S3_SECRET_KEY")
    );

    // S3Client 바로 생성 후 반환
    return S3Client.builder()
        .region(Region.of(props.getProperty("AWS_S3_REGION")))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }

  // 파일 업로드
  @Test
  void upload() throws IOException {
    Properties props = loadEnv();
    String bucket = props.getProperty("AWS_S3_BUCKET");
    String key = "test.txt";

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    try (S3Client s3Client = buildClient(props)) {
      s3Client.putObject(request, RequestBody.fromString("S3 업로드 테스트"));
      System.out.println("업로드 성공. 파일명: " + key);
    }
  }

  // 파일 다운로드
  @Test
  void download() throws IOException {
    Properties props = loadEnv();
    String bucket = props.getProperty("AWS_S3_BUCKET");
    String key = "test.txt";

    GetObjectRequest request = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    try (S3Client s3Client = buildClient(props)) {
      ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(request);
      String content = new String(responseInputStream.readAllBytes());
      System.out.println("다운로드한 내용: " + content);
    }
  }

  // PresignedUrl 생성
  @Test
  void presignedUrl() throws IOException {
    Properties props = loadEnv();
    String bucket = props.getProperty("AWS_S3_BUCKET");
    String key = "test.txt";

    AwsBasicCredentials credentials = AwsBasicCredentials.create(
        props.getProperty("AWS_S3_ACCESS_KEY"),
        props.getProperty("AWS_S3_SECRET_KEY")
    );

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .getObjectRequest(getObjectRequest)
        .signatureDuration(Duration.ofMinutes(2))
        .build();

    try (S3Presigner s3Presigner = S3Presigner.builder()
        .region(Region.of(props.getProperty("AWS_S3_REGION")))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build()) {
      String signed = s3Presigner.presignGetObject(presignRequest).url().toString();
      System.out.println("프리사인드 링크: " + signed);
    }
  }
}