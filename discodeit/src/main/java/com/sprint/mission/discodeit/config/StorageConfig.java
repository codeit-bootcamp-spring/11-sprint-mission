package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.storage.LocalBinaryContentStorage;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
    public LocalBinaryContentStorage localBinaryContentStorage(
            @Value("${discodeit.storage.local.root-path}") String rootPath) {
        return new LocalBinaryContentStorage(Paths.get(rootPath));
    }
}
