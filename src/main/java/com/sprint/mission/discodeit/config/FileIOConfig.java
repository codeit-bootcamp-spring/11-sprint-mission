package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.util.FileIOUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileIOConfig {

  @Value("${discodeit.repository.file-directory}")
  private String rootDirectory;

  @Value("${discodeit.repository.ddl-auto}")
  private String ddlAuto;

  @PostConstruct
  public void init() {
    FileIOUtil.setInitValue(rootDirectory, ddlAuto);
  }
}
