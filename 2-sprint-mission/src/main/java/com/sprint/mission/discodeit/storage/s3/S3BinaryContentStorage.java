package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.config.MDCLoggingInterceptor;
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.StorageOperationException;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
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

  private final String bucket;

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final ApplicationEventPublisher eventPublisher;

  @Value("${discodeit.storage.s3.presigned-url-expiration:600}")
  private long presignedUrlExpirationSeconds;

  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.access-key}") String accessKey,
      @Value("${discodeit.storage.s3.secret-key}") String secretKey,
      @Value("${discodeit.storage.s3.region}") String region,
      @Value("${discodeit.storage.s3.bucket}") String bucket,
      ApplicationEventPublisher eventPublisher
  ) {
    this.bucket = bucket;
    this.eventPublisher = eventPublisher;

    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

    this.s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(credentialsProvider)
        .build();

    this.s3Presigner = S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(credentialsProvider)
        .build();
  }

  @PreDestroy
  public void close() {
    if (this.s3Client != null) {
      this.s3Client.close();
    }
    if (this.s3Presigner != null) {
      this.s3Presigner.close();
    }
  }

  @Retryable(
      retryFor = StorageOperationException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  @Override
  public UUID put(UUID binaryContentId, byte[] bytes) {
    String key = binaryContentId.toString();
    try {
      String contentType = URLConnection.guessContentTypeFromStream(
          new ByteArrayInputStream(bytes));
      if (contentType == null) {
        contentType = "application/octet-stream";
      }

      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .contentType(contentType)
          .build();

      s3Client.putObject(request, RequestBody.fromBytes(bytes));
      log.info("S3 파일 업로드 성공: {}", key);
      return binaryContentId;
    } catch (S3Exception | IOException e) {
      log.error("S3 파일 업로드 실패: {}", key, e);
      throw StorageOperationException.uploadFailed(e);
    }
  }

  @Recover
  public UUID recover(StorageOperationException e, UUID binaryContentId, byte[] bytes) {
    String requestId = MDC.get(MDCLoggingInterceptor.REQUEST_ID);
    String errorMessage = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();

    log.error("S3 업로드 재시도 모두 실패: binaryContentId={}, requestId={}",
        binaryContentId, requestId);

    eventPublisher.publishEvent(
        new S3UploadFailedEvent(requestId, binaryContentId, errorMessage));

    throw e;
  }

  @Override
  public InputStream get(UUID binaryContentId) {
    String key = binaryContentId.toString();
    try {
      GetObjectRequest request = GetObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build();

      InputStream inputStream = s3Client.getObject(request, ResponseTransformer.toInputStream());
      log.info("S3 파일 조회 성공: {}", key);

      return inputStream;
    } catch (S3Exception e) {
      if (e.statusCode() == 404) {
        log.error("S3 파일 조회 실패 - 존재하지 않는 파일: {}", key);
        throw BinaryContentNotFoundException.withId(binaryContentId);
      }
      log.error("S3 파일 조회 실패: {}", key);
      throw StorageOperationException.readFailed();
    }
  }

  @Override
  public ResponseEntity<Void> download(BinaryContentDto.Response response) {
    String key = response.id().toString();
    try {
      String presignedUrl = generatePresignedUrl(key, response.contentType());

      log.info("Presigned URL 생성 성공: {}", key);

      return ResponseEntity
          .status(HttpStatus.FOUND)
          .location(URI.create(presignedUrl))
          .build();
    } catch (Exception e) {
      log.error("Presigned URL 생성 실패: {}", key, e);
      throw StorageOperationException.presignedUrlGenerationFailed();
    }
  }

  @Override
  public void delete(UUID id) {
    String key = id.toString();
    try {
      DeleteObjectRequest request = DeleteObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build();

      s3Client.deleteObject(request);
      log.info("S3 파일 삭제 성공: {}", key);
    } catch (S3Exception e) {
      log.error("S3 파일 삭제 실패: {}", key, e);
      throw StorageOperationException.deleteFailed();
    }
  }

  private String generatePresignedUrl(String key, String contentType) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .responseContentType(contentType)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofSeconds(presignedUrlExpirationSeconds))
        .getObjectRequest(getObjectRequest)
        .build();

    PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
    return presignedRequest.url().toString();
  }
}