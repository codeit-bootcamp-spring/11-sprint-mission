package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.retry.AdminFailureNotifier;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
@Component
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final String accessKey;
  private final String secretKey;
  private final String region;
  private final String bucket;
  private final AdminFailureNotifier adminFailureNotifier;

  @Value("${discodeit.storage.s3.presigned-url-expiration:600}") // 기본값 10분
  private long presignedUrlExpirationSeconds;

  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.access-key}") String accessKey,
      @Value("${discodeit.storage.s3.secret-key}") String secretKey,
      @Value("${discodeit.storage.s3.region}") String region,
      @Value("${discodeit.storage.s3.bucket}") String bucket,
      AdminFailureNotifier adminFailureNotifier
  ) {
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.region = region;
    this.bucket = bucket;
    this.adminFailureNotifier = adminFailureNotifier;
  }

  // S3 업로드는 네트워크 문제 등 일시적인 오류로 실패할 수 있어 자동 재시도를 적용합니다.
  // S3Exception이 그대로 전파되어야 재시도 대상으로 인식되므로, 이 메소드 내부에서는 감싸지 않습니다.
  @Retryable(
      retryFor = S3Exception.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 500, multiplier = 2, maxDelay = 2000)
  )
  @Override
  public UUID put(UUID binaryContentId, byte[] bytes) {
    String key = binaryContentId.toString();
    S3Client s3Client = getS3Client();

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    s3Client.putObject(request, RequestBody.fromBytes(bytes));
    log.info("S3에 파일 업로드 성공: {}", key);

    return binaryContentId;
  }

  // 재시도가 모두 실패했을 때 호출됩니다. 관리자에게 실패 사실을 통지한 뒤,
  // 예외를 다시 던져 BinaryContentEventListener가 상태를 FAIL로 갱신하도록 합니다.
  @Recover
  public UUID recoverPut(S3Exception e, UUID binaryContentId, byte[] bytes) {
    log.error("S3에 파일 업로드 재시도 모두 실패: binaryContentId={}, error={}",
        binaryContentId, e.getMessage());
    adminFailureNotifier.notifyBinaryContentFailure("S3 파일 업로드", binaryContentId, e);
    throw new RuntimeException("S3에 파일 업로드 실패(재시도 소진): " + binaryContentId, e);
  }

  @Override
  public InputStream get(UUID binaryContentId) {
    String key = binaryContentId.toString();
    try {
      S3Client s3Client = getS3Client();

      GetObjectRequest request = GetObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build();

      byte[] bytes = s3Client.getObjectAsBytes(request).asByteArray();
      return new ByteArrayInputStream(bytes);
    } catch (S3Exception e) {
      log.error("S3에서 파일 다운로드 실패: {}", e.getMessage());
      throw new NoSuchElementException("File with key " + key + " does not exist");
    }
  }

  private S3Client getS3Client() {
    return S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            )
        )
        .build();
  }

  @Override
  public ResponseEntity<Void> download(BinaryContentDto metaData) {
    try {
      String key = metaData.id().toString();
      String presignedUrl = generatePresignedUrl(key, metaData.contentType());

      log.info("생성된 Presigned URL: {}", presignedUrl);

      return ResponseEntity
          .status(HttpStatus.FOUND)
          .header(HttpHeaders.LOCATION, presignedUrl)
          .build();
    } catch (Exception e) {
      log.error("Presigned URL 생성 실패: {}", e.getMessage());
      throw new RuntimeException("Presigned URL 생성 실패", e);
    }
  }

  private String generatePresignedUrl(String key, String contentType) {
    try (S3Presigner presigner = getS3Presigner()) {
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .responseContentType(contentType)
          .build();

      GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
          .signatureDuration(Duration.ofSeconds(presignedUrlExpirationSeconds))
          .getObjectRequest(getObjectRequest)
          .build();

      PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
      return presignedRequest.url().toString();
    }
  }

  private S3Presigner getS3Presigner() {
    return S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            )
        )
        .build();
  }
} 