package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
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

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

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
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final long presignedUrlExpiration;

    // TODO: 생성자를 작성하세요.
    // @Value로 application.yaml의 설정값을 주입받습니다.
    // 주입받을 값: access-key, secret-key, region, bucket, presigned-url-expiration
    public S3BinaryContentStorage(
            @Value("${discodeit.storage.s3.access-key}") String accessKey,
            @Value("${discodeit.storage.s3.secret-key}") String secretKey,
            @Value("${discodeit.storage.s3.region}") String region,
            @Value("${discodeit.storage.s3.bucket}") String bucket,
            @Value("${discodeit.storage.s3.presigned-url-expiration}") long presignedUrlExpiration
    ) {
        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );

        // S3Client → 실제 업로드/다운로드용
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .build();

        // S3Presigner → URL 서명 전용
        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .build();

        this.bucket = bucket;
        this.presignedUrlExpiration = presignedUrlExpiration;
    }

    // TODO: S3에 파일을 저장하고 id를 반환하세요.
    // BinaryContent의 id를 S3 키(파일 경로)로 사용합니다.
    @Override
    public UUID put(UUID id, byte[] bytes) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(id.toString())  // UUID를 S3 키(파일 경로)로 사용
                        .build(),
                RequestBody.fromBytes(bytes)
        );
        return id;
    }

    // TODO: UUID 키로 S3에서 파일을 읽어 InputStream으로 반환하세요.
    @Override
    public InputStream get(UUID id) {
        return s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(id.toString())
                        .build()
        );
    }

    // TODO: PresignedUrl 방식으로 구현하세요.
    // 파일을 직접 응답에 담지 않고 302 리다이렉트를 사용합니다.
    // 클라이언트는 Location 헤더의 URL(S3 PresignedUrl)로 재요청합니다.
    @Override
    public ResponseEntity<?> download(BinaryContentDto dto) {
        String presignedUrl = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
                        .getObjectRequest(
                                GetObjectRequest.builder()
                                        .bucket(bucket)
                                        .key(dto.id().toString())
                                        .build()
                        )
                        .build()
        ).url().toString();

        // 302 리다이렉트 → 클라이언트가 S3 PresignedUrl로 직접 접근
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, presignedUrl)
                .build();
    }
}