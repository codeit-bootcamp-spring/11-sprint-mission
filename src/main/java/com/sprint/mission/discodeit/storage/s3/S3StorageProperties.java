package com.sprint.mission.discodeit.storage.s3;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties(prefix = "discodeit.storage.s3")
public record S3StorageProperties(
    String accessKey,
    String secretKey,
    String region,
    String bucket,
    @DurationUnit(ChronoUnit.SECONDS) Duration presignedUrlExpiration
) {

}
