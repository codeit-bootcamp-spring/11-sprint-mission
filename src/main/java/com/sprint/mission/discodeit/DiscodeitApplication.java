package com.sprint.mission.discodeit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@Slf4j
@EnableJpaAuditing // @CreatedDate, @LastModifiedDate 설정을 위해(JPA Auditing 활성화)
public class DiscodeitApplication {


  public static void main(String[] args) {

    SpringApplication.run(DiscodeitApplication.class, args);

    // Basic Service
    // 서비스 초기화
//    UserService userService = context.getBean(UserService.class);
//    ChannelService channelService = context.getBean(ChannelService.class);
//    MessageService messageService = context.getBean(MessageService.class);
//    UserStatusService userStatusService = context.getBean(UserStatusService.class);
//    ReadStatusService readStatusService = context.getBean(ReadStatusService.class);
//    BinaryContentService binaryContentService = context.getBean(BinaryContentService.class);
  }
}