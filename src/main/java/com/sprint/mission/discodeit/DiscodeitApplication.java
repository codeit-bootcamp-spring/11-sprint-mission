package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.ReadStatusService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@Slf4j
@EnableJpaAuditing // @CreatedDate, @LastModifiedDate 설정을 위해(JPA Auditing 활성화)
public class DiscodeitApplication {


  public static void main(String[] args) throws IOException {

    ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class,
        args);

    // Basic Service
    // 서비스 초기화
    UserService userService = context.getBean(UserService.class);
    ChannelService channelService = context.getBean(ChannelService.class);
    MessageService messageService = context.getBean(MessageService.class);
    AuthService authService = context.getBean(AuthService.class);
    UserStatusService userStatusService = context.getBean(UserStatusService.class);
    ReadStatusService readStatusService = context.getBean(ReadStatusService.class);
    BinaryContentService binaryContentService = context.getBean(BinaryContentService.class);
  }
}