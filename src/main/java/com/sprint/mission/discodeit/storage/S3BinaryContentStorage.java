package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentDto;
import com.sprint.mission.discodeit.exception.service.file.NonExistFileException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
// STORAGE_TYPE=s3 일 때만 이 Bean이 등록됨
// STORAGE_TYPE=local 이면 LocalBinaryContentStorage가 등록됨 (미션 6 구현체)
public class S3BinaryContentStorage implements BinaryContentStorage {

  // TODO: 필요한 필드를 선언하세요.
  // S3Client, S3Presigner, bucket 이름, presignedUrl 만료 시간이 필요합니다.
  // S3Client와 S3Presigner는 역할이 서로 다릅니다.
  // S3Client → 실제 파일 업로드/다운로드 API 호출
  // S3Presigner → URL 서명 전용. 실제 API 호출 없이 URL만 생성

  private final String bucketName;
  private final S3Client client;
  private final S3Presigner presigner;
  private final Duration presignedUrlDuration;


  // TODO: 생성자를 작성하세요.
  // @Value로 application.yaml의 설정값을 주입받습니다.
  // 주입받을 값: access-key, secret-key, region, bucket, presigned-url-expiration
  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.access-key}") String accessKey,
      @Value("${discodeit.storage.s3.secret-key}") String secretKey,
      @Value("${discodeit.storage.s3.region}") String region,
      @Value("${discodeit.storage.s3.bucket}") String bucketName,
      @Value("${discodeit.storage.s3.presigned-url-expiration}") int duration
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
  }


  // BinaryContent의 id를 S3 키(파일 경로)로 사용합니다.
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

  // TODO: UUID 키로 S3에서 파일을 읽어 InputStream으로 반환하세요.
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

  // TODO: PresignedUrl 방식으로 구현하세요.
  // 파일을 직접 응답에 담지 않고 302 리다이렉트를 사용합니다.
  // 클라이언트는 Location 헤더의 URL(S3 PresignedUrl)로 재요청합니다.
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

}