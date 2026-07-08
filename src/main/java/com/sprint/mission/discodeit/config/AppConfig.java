package com.sprint.mission.discodeit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableJpaAuditing // @CreatedDate, @LastModifiedDate 설정을 위해 JPA Auditing 활성화
@EnableScheduling // @Scheduled 사용을 위해 활성화
public class AppConfig {

}
