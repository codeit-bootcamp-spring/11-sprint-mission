package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.storage.LocalBinaryContentStorage;
import com.sprint.mission.discodeit.storage.S3BinaryContentStorage;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local", matchIfMissing = true)
    public LocalBinaryContentStorage localBinaryContentStorage(
            @Value("${discodeit.storage.local.root-path}") String rootPath) {
        return new LocalBinaryContentStorage(Paths.get(rootPath));
    }

    @Bean
    @ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
    public S3BinaryContentStorage s3BinaryContentStorage(
            @Value("${discodeit.storage.s3.access-key}") String accessKey,
            @Value("${discodeit.storage.s3.secret-key}") String secretKey,
            @Value("${discodeit.storage.s3.region}") String region,
            @Value("${discodeit.storage.s3.bucket}") String bucket,
            @Value("${discodeit.storage.s3.presigned-url-expiration}") long presignedUrlExpiration) {
        return new S3BinaryContentStorage(accessKey, secretKey, region, bucket, presignedUrlExpiration);
    }
}
