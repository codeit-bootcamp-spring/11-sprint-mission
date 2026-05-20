package com.sprint.mission.discodeit.storage.s3;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
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

@Disabled("CI 환경에서는 실행하지 않음")
public class AWSS3Test {

  private static final String TEST_KEY = UUID.randomUUID().toString();

  private static Properties loadEnv() throws IOException {
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream(".env")) {
      props.load(fis);
    }
    return props;
  }

  private static S3Client buildClient(Properties props) {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(
        props.getProperty("AWS_S3_ACCESS_KEY"),
        props.getProperty("AWS_S3_SECRET_KEY")
    );

    return S3Client.builder()
        .region(Region.of(props.getProperty("AWS_S3_REGION")))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }

  @Test
  void upload() throws IOException {
    Properties props = loadEnv();
    S3Client s3Client = buildClient(props);
    String bucket = props.getProperty("AWS_S3_BUCKET");

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucket)
        .key(TEST_KEY)
        .contentType("text/plain")
        .contentDisposition("attachment; filename=\"sample.txt\"")
        .build();

    s3Client.putObject(request, RequestBody.fromString("S3 key = " + TEST_KEY));
    System.out.println("업로드 성공 - key : " + TEST_KEY);
  }

  @Test
  void download() throws IOException {
    Properties props = loadEnv();
    S3Client s3Client = buildClient(props);
    String bucket = props.getProperty("AWS_S3_BUCKET");

    GetObjectRequest request = GetObjectRequest.builder()
        .bucket(bucket)
        .key(TEST_KEY)
        .build();

    ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
    String content = new String(response.readAllBytes());
    System.out.println("다운로드 성공 - 내용 : " + content);
  }

  // TODO: PresignedUrl 생성 테스트를 작성하세요.
  // 10분간 유효한 URL을 생성하고 콘솔에 출력합니다.
  // S3Client가 아닌 S3Presigner를 별도로 생성해야 합니다.
  // 생성된 URL을 브라우저에 붙여넣어서 실제로 파일이 다운로드되는지 확인해보세요.
  @Test
  void presignedUrl() throws IOException {
    Properties props = loadEnv();
    String bucket = props.getProperty("AWS_S3_BUCKET");

    AwsBasicCredentials credentials = AwsBasicCredentials.create(
        props.getProperty("AWS_S3_ACCESS_KEY"),
        props.getProperty("AWS_S3_SECRET_KEY")
    );

    S3Presigner preSigner = S3Presigner.builder()
        .region(Region.of(props.getProperty("AWS_S3_REGION")))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(10))
        .getObjectRequest(GetObjectRequest.builder()
            .bucket(bucket)
            .key(TEST_KEY)
            .build())
        .build();

    PresignedGetObjectRequest presignedGetObjectRequest = preSigner.presignGetObject(
        presignRequest);
    System.out.println("URL : " + presignedGetObjectRequest.url());
  }
}

