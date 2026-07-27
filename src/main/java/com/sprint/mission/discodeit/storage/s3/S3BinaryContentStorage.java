package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.event.notification.S3UploadFailedEvent;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
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
@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
// name이 "s3"일때만 이 Bean을 등록
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final S3Client s3Client;
  private final S3Presigner presigner;


  private final String accessKey;
  private final String secretKey;
  private final String region;
  private final String bucket;
  private final long expiration;

  private final ApplicationEventPublisher eventPublisher;

  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.access-key}") String accessKey,
      @Value("${discodeit.storage.s3.secret-key}") String secretKey,
      @Value("${discodeit.storage.s3.region}") String region,
      @Value("${discodeit.storage.s3.bucket}") String bucket,
      @Value("${discodeit.storage.s3.presigned-url-expiration}") long expiration,
      ApplicationEventPublisher eventPublisher) {
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.region = region;
    this.bucket = bucket;
    this.expiration = expiration;
    this.eventPublisher = eventPublisher;

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

  @Override
  @Retryable(
      maxAttempts = 4, // 첫 실패 후 3회(재시도 횟수 정책, default 3)
      backoff = @Backoff(delay = 1000, multiplier = 2) // 1, 2, 4초 후 재시도(대기시간 정책)
  )
  public UUID put(UUID id, byte[] bytes) {

    try {
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(bucket)
          .key(id.toString())
          .build();

      s3Client.putObject(request, RequestBody.fromBytes(bytes));

      return id;
    } catch (S3Exception e) {
      log.error("S3에 파일 업로드 실패", e);
      throw new RuntimeException("S3에 파일 업로드 실패 : " + id.toString(), e);
    }
  }

  @Recover
  // Retryable 실패 시 이 메서드를 실행
  // @Recover 추적을 쉽게 하기 위해서 이 메서드에 사용되지 않는 byte[] 타입도 같이 매개변수로 설정
  public UUID recover(
      Exception e,
      UUID binaryContentId,
      byte[] bytes
  ) {

    eventPublisher.publishEvent(new S3UploadFailedEvent(binaryContentId, e));

    return binaryContentId;
  }

  @Override
  public InputStream get(UUID id) {

    try {
      GetObjectRequest request = GetObjectRequest.builder()
          .bucket(bucket)
          .key(id.toString())
          .build();

      return s3Client.getObject(request);
    } catch (S3Exception e) {
      log.error("S3에서 파일 다운로드 실패", e);
      throw new NoSuchElementException("File with key " + id.toString() + " does not exist");
    }
  }

  @Override
  public ResponseEntity<Void> download(BinaryContentDto dto) {
    String url = generatePresignedUrl(dto.id().toString(), dto.contentType());

    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
  }

  private String generatePresignedUrl(String key, String contentType) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .responseContentType(contentType)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofSeconds(expiration))
        .getObjectRequest(getObjectRequest)
        .build();

    PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

    return presignedRequest.url().toString();
  }
}
