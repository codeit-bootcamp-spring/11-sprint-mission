package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RepositoryConfig {

  @Value("${discodeit.repository.file-directory:.discodeit}")
  private String fileDirectory;

  @Bean
  public FileLockProvider fileLockProvider() {
    return new FileLockProvider();
  }

  @Bean
  @Primary
  public UserRepository userRepository(FileLockProvider fileLockProvider) {
    new File(fileDirectory).mkdirs(); // 디렉토리 없으면 생성
    return new FileUserRepository(fileDirectory, fileLockProvider);
  }

  @Bean
  @Primary
  public ChannelRepository channelRepository(
      FileLockProvider fileLockProvider,
      ReadStatusRepository readStatusRepository) {
    new File(fileDirectory).mkdirs();
    return new FileChannelRepository(fileDirectory, fileLockProvider, readStatusRepository);
  }

  @Bean
  @Primary
  public MessageRepository messageRepository(FileLockProvider fileLockProvider) {
    new File(fileDirectory).mkdirs();
    return new FileMessageRepository(fileDirectory, fileLockProvider);
  }
}