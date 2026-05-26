package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.StorageOperationException;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
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

  private final String accessKey;
  private final String secretKey;
  private final String region;
  private final String bucket;

  @Value("${discodeit.storage.s3.presigned-url-expiration:600}")
  private long presignedUrlExpirationSeconds;

  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.access-key}") String accessKey,
      @Value("${discodeit.storage.s3.secret-key}") String secretKey,
      @Value("${discodeit.storage.s3.region}") String region,
      @Value("${discodeit.storage.s3.bucket}") String bucket
  ) {
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.region = region;
    this.bucket = bucket;
  }

  @Override
  public UUID put(UUID binaryContentId, byte[] bytes) {
    String key = binaryContentId.toString();
    try (S3Client s3Client = getS3Client()) {
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build();

      s3Client.putObject(request, RequestBody.fromBytes(bytes));
      log.info("S3 파일 업로드 성공: {}", key);

      return binaryContentId;
    } catch (S3Exception e) {
      log.error("S3 파일 업로드 실패: {}", key, e);
      throw StorageOperationException.uploadFailed();
    }
  }

  @Override
  public InputStream get(UUID binaryContentId) {
    String key = binaryContentId.toString();
    try (S3Client s3Client = getS3Client()) {
      GetObjectRequest request = GetObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build();

      byte[] bytes = s3Client.getObjectAsBytes(request).asByteArray();
      log.info("S3 파일 조회 성공: {}", key);

      return new ByteArrayInputStream(bytes);
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
    try (S3Client s3Client = getS3Client()) {
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

  private S3Client getS3Client() {
    return S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey)
        ))
        .build();
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
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey)
        ))
        .build();
  }
}