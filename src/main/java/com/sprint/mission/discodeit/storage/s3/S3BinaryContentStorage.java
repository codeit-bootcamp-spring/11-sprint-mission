package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.exception.StorageException;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final Duration presignedUrlExpiration;

    public S3BinaryContentStorage(
            @Value("${discodeit.storage.s3.access-key}") String accessKey,
            @Value("${discodeit.storage.s3.secret-key}") String secretKey,
            @Value("${discodeit.storage.s3.region}") String region,
            @Value("${discodeit.storage.s3.bucket}") String bucket,
            @Value("${discodeit.storage.s3.presigned-url-expiration}") long presignedUrlExpirationSeconds
    ) {
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

        this.bucket = bucket;
        this.presignedUrlExpiration = Duration.ofSeconds(presignedUrlExpirationSeconds);
    }

    @Override
    public UUID put(UUID id, byte[] bytes) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(toKey(id))
                            .build(),
                    RequestBody.fromBytes(bytes)
            );

            return id;
        } catch (Exception e) {
            throw new StorageException("S3 바이너리 데이터 저장에 실패했습니다.", e);
        }
    }

    @Override
    public InputStream get(UUID id) {
        try {
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(toKey(id))
                            .build()
            );

            return response;
        } catch (NoSuchKeyException e) {
            throw new StorageException("S3에서 파일을 찾을 수 없습니다. id=" + id, e);
        } catch (Exception e) {
            throw new StorageException("S3 바이너리 데이터 조회에 실패했습니다.", e);
        }
    }

    @Override
    public ResponseEntity<?> download(BinaryContentDto binaryContentDto) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(toKey(binaryContentDto.id()))
                    .responseContentType(binaryContentDto.contentType())
                    .responseContentDisposition(
                            "attachment; filename=\"" + binaryContentDto.fileName() + "\""
                    )
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(presignedUrlExpiration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(presignedRequest.url().toString()))
                    .build();
        } catch (Exception e) {
            throw new StorageException("S3 Presigned URL 생성에 실패했습니다.", e);
        }
    }

    @Override
    public void delete(UUID id) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(toKey(id))
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("S3 바이너리 데이터 삭제에 실패했습니다.", e);
        }
    }

    @PreDestroy
    public void close() {
        s3Client.close();
        s3Presigner.close();
    }

    private String toKey(UUID id) {
        return id.toString();
    }
}