package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.service.dto.binarycontent.BinaryContentDto;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class S3BinaryContentStorage implements BinaryContentStorage {

    private final String accessKey;
    private final String secretKey;
    private final String region;
    private final String bucket;
    private final long presignedUrlExpiration;

    public S3BinaryContentStorage(String accessKey, String secretKey, String region, String bucket,
            long presignedUrlExpiration) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.bucket = bucket;
        this.presignedUrlExpiration = presignedUrlExpiration;
    }

    @Override
    public UUID put(UUID id, byte[] data) {
        getS3Client().putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(id.toString())
                        .build(),
                RequestBody.fromBytes(data == null ? new byte[0] : data)
        );
        return id;
    }

    @Override
    public InputStream get(UUID id) {
        return getS3Client().getObject(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(id.toString())
                        .build()
        );
    }

    @Override
    public ResponseEntity<Void> download(BinaryContentDto dto) {
        String url = generatePresignedUrl(dto.id().toString(), dto.contentType());
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
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
        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build()) {
            return presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
                            .getObjectRequest(GetObjectRequest.builder()
                                    .bucket(bucket)
                                    .key(key)
                                    .build())
                            .build()
            ).url().toString();
        }
    }
}
