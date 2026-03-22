package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.util.FileIOUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
public class FileIOConfig {
    @Value("${discodeit.repository.file-directory}")
    private String rootDirectory;

    @PostConstruct
    public void init() {
        FileIOUtil.setRootDirectory(rootDirectory);
    }
}
