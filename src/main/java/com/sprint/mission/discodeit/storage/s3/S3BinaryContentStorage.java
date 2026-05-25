package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
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

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucket;
  private final long presignedUrlExpiration;


  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.access-key}") String accessKey,
      @Value("${discodeit.storage.s3.secret-key}") String secretKey,
      @Value("${discodeit.storage.s3.region}") String region,
      @Value("${discodeit.storage.s3.bucket}") String bucket,
      @Value("${discodeit.storage.s3.presigned-url-expiration}") Long presignedUrlExpiration) {
    this.bucket = bucket;
    this.presignedUrlExpiration = presignedUrlExpiration;

    AwsBasicCredentials credentials = AwsBasicCredentials.create(
        accessKey, secretKey
    );

    this.s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();

    this.s3Presigner = S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }

  @Override
  public UUID put(UUID id, byte[] bytes) {
    String key = id.toString();

    PutObjectRequest request = PutObjectRequest.builder()
        .key(key)
        .bucket(bucket)
        .build();

    s3Client.putObject(request, RequestBody.fromBytes(bytes));
    return id;
  }

  @Override
  public InputStream get(UUID id) {
    String key = id.toString();

    GetObjectRequest request = GetObjectRequest.builder()
        .key(key)
        .bucket(bucket)
        .build();

    return s3Client.getObject(request); // 스트림 형태로 그대로 반환
  }

  @Override
  public ResponseEntity<?> download(BinaryContentDto binaryContentDto) {
    String key = binaryContentDto.id().toString();

    GetObjectRequest request = GetObjectRequest.builder()
        .key(key)
        .bucket(bucket)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .getObjectRequest(request)
        .signatureDuration(Duration.ofMinutes(presignedUrlExpiration))
        .build();

    String signedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

    return ResponseEntity.status(HttpStatus.FOUND) // 302 리다이렉트 상태 코드
        .location(URI.create(signedUrl))
        .build();
  }
}
