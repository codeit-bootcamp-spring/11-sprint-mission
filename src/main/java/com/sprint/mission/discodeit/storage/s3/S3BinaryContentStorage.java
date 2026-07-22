package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.binarycontentdto.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.FileException;
import com.sprint.mission.discodeit.exception.service.file.NonExistFileException;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
@Slf4j
// STORAGE_TYPE=s3 일 때만 이 Bean이 등록됨
// STORAGE_TYPE=local 이면 LocalBinaryContentStorage가 등록됨 (미션 6 구현체)
public class S3BinaryContentStorage implements BinaryContentStorage {


  private final String bucketName;
  private final S3Client client;
  private final S3Presigner presigner;
  private final Duration presignedUrlDuration;
  private final ApplicationEventPublisher applicationEventPublisher;

  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.access-key}") String accessKey,
      @Value("${discodeit.storage.s3.secret-key}") String secretKey,
      @Value("${discodeit.storage.s3.region}") String region,
      @Value("${discodeit.storage.s3.bucket}") String bucketName,
      @Value("${discodeit.storage.s3.presigned-url-expiration}") int duration,

      ApplicationEventPublisher applicationEventPublisher
  ) {
    this.bucketName = bucketName;
    this.presignedUrlDuration = Duration.ofMinutes(duration);
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

    this.client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
    this.presigner = S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();

    this.applicationEventPublisher = applicationEventPublisher;
  }


  // BinaryContent의 id를 S3 키(파일 경로)로 사용합니다.
  @Retryable(
      retryFor = {SdkClientException.class, S3Exception.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  @Override
  public UUID put(UUID id, byte[] bytes) {
    String key = id.toString();

    PutObjectRequest putReq = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

    client.putObject(putReq, RequestBody.fromBytes(bytes));

    return id;

  }


  @Override
  public InputStream get(UUID id) {

    String key = id.toString();

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

    try {

      return client.getObject(getObjectRequest,
          ResponseTransformer.toInputStream());

    } catch (NoSuchKeyException e) {
      throw new NonExistFileException(id);
    }
  }

  @Override
  public ResponseEntity<?> download(BinaryContentDto dto) {
    String key = dto.id().toString();

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(presignedUrlDuration) // 앞서 설정한 10분(Duration.ofMinutes(10))
        .getObjectRequest(getObjectRequest)
        .build();

    PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
    String presignedUrl = presignedRequest.url().toString();

    return ResponseEntity.status(302).header("Location", presignedUrl).build();


  }

  @Recover
  public UUID recover(Exception e, UUID id, byte[] bytes) {
    String requestId = MDC.get("requestId");

    log.error("S3 업로드 실패, requestId: {}, binaryContentId: {}, error: {}",
        requestId, id, e.getMessage());

    applicationEventPublisher.publishEvent(
        new S3UploadFailedEvent("S3 Binary Upload", requestId, id, e.getMessage())
    );

    throw new FileException(ErrorCode.FILE_PUT_ERROR, Map.of("binaryContentId", id));
  }


}