package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.BinaryContentUploadException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
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

@Slf4j
@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucket;
    private final long presignedUrlExpiration;
    private final ApplicationEventPublisher eventPublisher;

    public S3BinaryContentStorage(
            @Value("${discodeit.storage.s3.access-key}") String accessKey,
            @Value("${discodeit.storage.s3.secret-key}") String secretKey,
            @Value("${discodeit.storage.s3.region}") String region,
            @Value("${discodeit.storage.s3.bucket}") String bucket,
            @Value("${discodeit.storage.s3.presigned-url-expiration}") long presignedUrlExpiration,
            ApplicationEventPublisher eventPublisher) {

        this.bucket = bucket;
        this.presignedUrlExpiration = presignedUrlExpiration;
        this.eventPublisher = eventPublisher;

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();

        this.presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
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

    // 재시도가 모두 실패했을 때 호출됨 (원본 메소드와 동일한 파라미터 + 첫 인자로 예외)
    // 주의: 정상값(UUID)을 그냥 return하면 호출부(BinaryContentUploadEventListener.on())가
    // put()이 성공한 것으로 착각해 상태를 SUCCESS로 잘못 갱신하게 된다.
    // 관리자 알림을 발행한 뒤 반드시 예외를 다시 던져 호출부의 catch 블록이 FAIL 상태로
    // 갱신하도록 해야 한다.
    @Recover
    public UUID recover(Exception e, UUID id, byte[] bytes) {
        String requestId = MDC.get("requestId");
        log.error("S3 바이너리 데이터 저장 최종 실패 - requestId: {}, binaryContentId: {}", requestId, id, e);

        eventPublisher.publishEvent(new S3UploadFailedEvent(
                "binaryContentUpload", requestId, id, e.getMessage()
        ));
        throw new BinaryContentUploadException("S3 바이너리 데이터 저장 최종 실패 - binaryContentId: " + id, e);
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
    public ResponseEntity<?> download(BinaryContentDto dto) {
        String url = presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(dto.id().toString())
                                .build())
                        .build()
        ).url().toString();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }
}
