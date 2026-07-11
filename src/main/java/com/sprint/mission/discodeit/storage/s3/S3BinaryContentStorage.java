package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.annotation.PreDestroy;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final S3StorageProperties properties;
  private final S3Client s3Client;
  private final S3Presigner presigner;
  private final ApplicationEventPublisher applicationEventPublisher;

  public S3BinaryContentStorage(S3StorageProperties properties,
      ApplicationEventPublisher applicationEventPublisher) {
    this.properties = properties;
    this.applicationEventPublisher = applicationEventPublisher;

    StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(properties.accessKey(), properties.secretKey()));

    this.s3Client = S3Client.builder()
        .region(Region.of(properties.region()))
        .credentialsProvider(credentialsProvider)
        .build();

    this.presigner = S3Presigner.builder()
        .region(Region.of(properties.region()))
        .credentialsProvider(credentialsProvider)
        .build();
  }

  private String generatePresignedUrl(String key, String contentType) {
    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(properties.presignedUrlExpiration())
        .getObjectRequest(GetObjectRequest.builder()
            .bucket(properties.bucket())
            .key(key)
            .responseContentType(contentType)
            .build())
        .build();

    return presigner.presignGetObject(presignRequest).url().toString();
  }

  @Retryable(
      retryFor = {SdkException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 500, multiplier = 2)
  )
  @Override
  public UUID put(UUID id, byte[] bytes) {
    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(properties.bucket())
        .key(id.toString())
        .build();

    s3Client.putObject(request, RequestBody.fromBytes(bytes));
    return id;
  }

  @Recover
  public UUID recover(Exception e, UUID id, byte[] bytes) {
    String requestId = MDC.get("requestId");
    applicationEventPublisher.publishEvent(
        new S3UploadFailedEvent(requestId, id, e.getMessage())
    );
    return id;
  }

  @Override
  public InputStream get(UUID id) {
    GetObjectRequest request = GetObjectRequest.builder()
        .bucket(properties.bucket())
        .key(id.toString())
        .build();

    return s3Client.getObject(request);
  }

  @Override
  public ResponseEntity<Resource> download(BinaryContentDto dto) {
    String url = generatePresignedUrl(dto.id().toString(), dto.contentType());
    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(url))
        .build();
  }

  @PreDestroy
  public void close() {
    s3Client.close();
    presigner.close();
  }
}
