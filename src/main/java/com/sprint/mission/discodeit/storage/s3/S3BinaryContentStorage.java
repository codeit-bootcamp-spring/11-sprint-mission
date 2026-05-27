package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
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
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
@Component
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final S3Client s3Client;
  private final S3Presigner presigner;
  private final String bucket;
  private final long presignedUrlExpiration;

  @Autowired
  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.access-key}") String accessKey,
      @Value("${discodeit.storage.s3.secret-key}") String secretKey,
      @Value("${discodeit.storage.s3.region}") String region,
      @Value("${discodeit.storage.s3.bucket}") String bucket,
      @Value("${discodeit.storage.s3.presigned-url-expiration}") long presignedUrlExpiration
  ) {
    this(
        buildS3Client(accessKey, secretKey, region),
        buildPresigner(accessKey, secretKey, region),
        bucket,
        presignedUrlExpiration
    );
  }

  private static S3Client buildS3Client(String accessKey, String secretKey, String region) {
    return S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(buildCredentialsProvider(accessKey, secretKey))
        .build();
  }

  private static S3Presigner buildPresigner(String accessKey, String secretKey, String region) {
    return S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(buildCredentialsProvider(accessKey, secretKey))
        .build();
  }

  private static StaticCredentialsProvider buildCredentialsProvider(String accessKey,
      String secretKey) {
    return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
  }

  S3BinaryContentStorage(S3Client s3Client, S3Presigner presigner, String bucket,
      long presignedUrlExpiration) {
    this.s3Client = s3Client;
    this.presigner = presigner;
    this.bucket = bucket;
    this.presignedUrlExpiration = presignedUrlExpiration;
  }

  @Override
  public UUID put(UUID binaryContentId, byte[] bytes) {
    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(binaryContentId.toString())
            .build(),
        RequestBody.fromBytes(bytes)
    );
    return binaryContentId;
  }

  @Override
  public InputStream get(UUID binaryContentId) {
    return s3Client.getObject(
        GetObjectRequest.builder()
            .bucket(bucket)
            .key(binaryContentId.toString())
            .build()
    );
  }

  @Override
  public ResponseEntity<?> download(BinaryContentDto metaData) {
    PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
            .getObjectRequest(GetObjectRequest.builder()
                .bucket(bucket)
                .key(metaData.id().toString())
                .build())
            .build()
    );

    return ResponseEntity
        .status(HttpStatus.FOUND)
        .location(URI.create(presignedRequest.url().toString()))
        .build();
  }
}
