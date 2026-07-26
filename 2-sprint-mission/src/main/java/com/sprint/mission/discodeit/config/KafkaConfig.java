package com.sprint.mission.discodeit.config;

import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("kafka")
public class KafkaConfig {

  @Bean
  public String realtimeGroupId() {
    return "discodeit-realtime-" + UUID.randomUUID();
  }
}