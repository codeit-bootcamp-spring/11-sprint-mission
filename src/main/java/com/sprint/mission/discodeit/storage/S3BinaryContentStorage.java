package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.service.NotificationService;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucket;
  private final long presignedUrlExpiration;
  private final NotificationService notificationService;

  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.access-key}") String accessKey,
      @Value("${discodeit.storage.s3.secret-key}") String secretKey,
      @Value("${discodeit.storage.s3.region}") String region,
      @Value("${discodeit.storage.s3.bucket}") String bucket,
      @Value("${discodeit.storage.s3.presigned-url-expiration}") long presignedUrlExpiration,
      NotificationService notificationService
  ) {
    this.bucket = bucket;
    this.presignedUrlExpiration = presignedUrlExpiration;
    this.notificationService = notificationService;

    AwsBasicCredentials credentials = AwsBasicCredentials
        .create(accessKey, secretKey);
    StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider
        .create(credentials);
    Region awsRegion = Region.of(region);

    this.s3Client = S3Client.builder().region(awsRegion)
        .credentialsProvider(credentialsProvider).build();
    this.s3Presigner = S3Presigner.builder().region(awsRegion)
        .credentialsProvider(credentialsProvider).build();
  }

  @Override
  @Retryable(
      retryFor = {Exception.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 2000)
  )
  public UUID put(UUID id, byte[] bytes) {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket).key(id.toString()).build();
    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
    return id;
  }

  @Recover
  public UUID recoverPut(Exception e, UUID id, byte[] bytes) {
    String requestId = MDC.get("requestId");

    String title = "S3 파일 업로드 실패";
    String content = String.format(
        "RequestId: %s\nBinaryContentId: %s\nError: %s",
        requestId != null ? requestId : "N/A",
        id.toString(), e.getMessage()
    );

    notificationService.notifyAdmins(title, content);

    throw new RuntimeException("S3 파일 업로드 최종 실패", e);
  }

  @Override
  public InputStream get(UUID id) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket).key(id.toString()).build();
    ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
        getObjectRequest);
    return response;
  }

  @Override
  public ResponseEntity<?> download(BinaryContentDto dto) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket).key(dto.id().toString()).build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
        .getObjectRequest(getObjectRequest).build();

    PresignedGetObjectRequest presignedRequest = s3Presigner
        .presignGetObject(presignRequest);
    String url = presignedRequest.url().toString();

    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(url)).build();
  }

  @Override
  public void delete(UUID id) {
    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
        .bucket(bucket).key(id.toString()).build();

    s3Client.deleteObject(deleteObjectRequest);
  }
}
