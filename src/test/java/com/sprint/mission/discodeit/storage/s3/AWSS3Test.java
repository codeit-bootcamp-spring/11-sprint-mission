package com.sprint.mission.discodeit.storage.s3;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

public class AWSS3Test {

  private S3Client s3Client;
  private S3Presigner presigner;
  private String bucket;

  @BeforeEach
  void setUp() throws Exception {
    Properties properties = new Properties();
    properties.load(new FileInputStream(".env"));

    String accessKey = properties.getProperty("AWS_S3_ACCESS_KEY"); // IAM 액세스 키
    String secretKey = properties.getProperty("AWS_S3_SECRET_KEY"); // IAM 비밀 액세스 키
    String region = properties.getProperty("AWS_S3_REGION"); // 리전(내 지역)
    bucket = properties.getProperty("AWS_S3_BUCKET"); // 버킷 이름

    // 자격 증명
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

    this.s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();

    this.presigner = S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }

  @Test
  @DisplayName("S3 업로드 테스트")
  void _S3_txt파일_업로드_테스트_() {
    // given
    String fileKey = "test/upload.txt";

    // 내 S3 버킷에 text 파일 생성 요청
    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucket)
        .key(fileKey)
        .contentType("text/plain")
        .build();

    // 프로젝트 내의 경로에 있는 txt파일
    Path target = Path.of("src/test/resources/example.txt");

    // when
    // 내 S3 버킷에 example.txt를 업로드
    s3Client.putObject(request, target);

    // then
    // 내 S3 버킷에 test/upload.txt가 있는지 확인
    HeadObjectResponse response = s3Client.headObject(
        HeadObjectRequest.builder()
            .bucket(bucket)
            .key(fileKey)
            .build()
    );

    assertNotNull(response);
  }

  @Test
  @DisplayName("S3 다운로드 테스트")
  void S3_txt파일_다운로드_테스트() throws Exception {
    // given
    String key = "test/upload.txt";

    // 내 S3 버킷에서 파일을 가져옴
    GetObjectRequest get = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    // 경로 설정
    Path target = Path.of("build/download.txt");

    // when
    // 이미 build/download.txt가 존재할 경우 삭제
    Files.deleteIfExists(target);

    // 내 S3 버킷에서 build 경로에 download.txt로 저장
    s3Client.getObject(get, target);

    // then
    assertTrue(target.toFile().exists());
  }

  // 다운로드 테스트에서 발생한 파일 삭제
  @AfterEach
  void tearDown() throws Exception {
    Files.deleteIfExists(Path.of("build/download.txt"));
  }

  @Test
  @DisplayName("Presigned URL 생성 테스트(20초 동안 유효)")
  void _20초_동안_유효한_txt파일_Presigned_URL_생성_테스트_() {
    // given
    String key = "test/upload.txt";

    // 내 S3 버킷에서 파일을 가져옴
    GetObjectRequest get = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    // Presigned 요청
    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofSeconds(20)) // 20초 동안 허용
        .getObjectRequest(get)
        .build();

    // when
    // Presigned 요청 가져오기
    PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

    // then
    // URL을 출력하여 20초 동안 유효한지 테스트
    String url = presignedRequest.url().toString();
    System.out.println("Presigned URL : " + url);

    assertNotNull(url);
  }
}
