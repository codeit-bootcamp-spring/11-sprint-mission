package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.event.binarycontent.S3UploadFailedEvent;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.storage.DownloadResult;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
@Component
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final String bucket;
  private final long presignedUrlExpiration;
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final ApplicationEventPublisher eventPublisher;

  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.access-key}") String accessKey,
      @Value("${discodeit.storage.s3.secret-key}") String secretKey,
      @Value("${discodeit.storage.s3.region}") String region,
      @Value("${discodeit.storage.s3.bucket}") String bucket,
      @Value("${discodeit.storage.s3.presigned-url-expiration}") long presignedUrlExpiration,
      ApplicationEventPublisher eventPublisher
  ) {
    this.bucket = bucket;
    this.presignedUrlExpiration = presignedUrlExpiration;
    this.eventPublisher = eventPublisher;

    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
    Region awsRegion = Region.of(region);

    this.s3Client = S3Client.builder()
        .region(awsRegion)
        .credentialsProvider(credentialsProvider)
        .build();

    this.s3Presigner = S3Presigner.builder()
        .region(awsRegion)
        .credentialsProvider(credentialsProvider)
        .build();
  }

  @Retryable(
      retryFor = SdkException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  @Override
  public UUID put(UUID id, byte[] bytes) {
    try {
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(id.toString())
              .build(),
          RequestBody.fromBytes(bytes)
      );
      log.info("S3 upload success: bucket={}, key={}, size={}", bucket, id, bytes.length);
      return id;
    } catch (SdkException e) {
      log.warn("S3 upload attempt fail: bucket={}, key={}, size={}, exceptionType={}, reason={}",
          bucket, id, bytes.length, e.getClass().getSimpleName(), e.getMessage(), e);
      throw e;
    }
  }

  @Recover
  public UUID recover(SdkException e, UUID id, byte[] bytes) {
    String requestId = MDC.get("requestId");
    log.error("S3 upload fail after retries exhausted: bucket={}, key={}, requestId={}, reason={}",
        bucket, id, requestId, e.getMessage(), e);

    this.eventPublisher.publishEvent(new S3UploadFailedEvent(requestId, id, e.getMessage()));
    throw e;
  }

  @Override
  public InputStream get(UUID id) {
    InputStream result = s3Client.getObject(
        GetObjectRequest.builder()
            .bucket(bucket)
            .key(id.toString())
            .build()
    );
    log.info("S3 download success: bucket={}, key={}", bucket, id);
    return result;
  }

  @Override
  public DownloadResult download(BinaryContentResponse dto) {
    String url = s3Presigner.presignGetObject(
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
            .getObjectRequest(GetObjectRequest.builder()
                .bucket(bucket)
                .key(dto.id().toString())
                .build())
            .build()
    ).url().toString();
    log.info("S3 presigned-url generate success: bucket={}, key={}, fileName={}",
        bucket, dto.id(), dto.fileName());
    return new DownloadResult.Redirect(url);
  }
}
