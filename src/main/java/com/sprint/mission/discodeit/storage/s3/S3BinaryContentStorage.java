package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.storage.DownloadResult;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
@Component
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final String bucket;
  private final long presignedUrlExpiration;
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.access-key}") String accessKey,
      @Value("${discodeit.storage.s3.secret-key}") String secretKey,
      @Value("${discodeit.storage.s3.region}") String region,
      @Value("${discodeit.storage.s3.bucket}") String bucket,
      @Value("${discodeit.storage.s3.presigned-url-expiration}") long presignedUrlExpiration
  ) {
    this.bucket = bucket;
    this.presignedUrlExpiration = presignedUrlExpiration;

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

  @Override
  public UUID put(UUID id, byte[] bytes) {
    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(id.toString())
            .build(),
        RequestBody.fromBytes(bytes)
    );
    return id;
  }

  @Override
  public InputStream get(UUID id) {
    return s3Client.getObject(
        GetObjectRequest.builder()
            .bucket(bucket)
            .key(id.toString())
            .build()
    );
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
    return new DownloadResult.Redirect(url);
  }
}
