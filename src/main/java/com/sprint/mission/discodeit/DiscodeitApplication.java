package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
@Slf4j
public class DiscodeitApplication {


    public static void main(String[] args) throws IOException {

        ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

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