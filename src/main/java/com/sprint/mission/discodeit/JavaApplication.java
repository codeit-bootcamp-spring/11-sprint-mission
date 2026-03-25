package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.repository.file.*;
import com.sprint.mission.discodeit.repository.jcf.*;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.service.basic.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(DiscodeitApplication.class, args);

        UserService userService = context.getBean(UserService.class);
        ChannelService channelService = context.getBean(ChannelService.class);
        MessageService messageService = context.getBean(MessageService.class);

        UserDto user1 = userService.create(
                new UserCreateRequest("Evan", "Evan@gmail.com", "1234", null)
        );
        UserDto user2 = userService.create(
                new UserCreateRequest("Rick", "Rick@gmail.com", "9876", null)
        );

        ChannelDto channel1 = channelService.createPublic(
                new PublicChannelCreateRequest("Test", "All chat")
        );

        MessageDto message1 = messageService.create(
                new MessageCreateRequest(user1.id(), channel1.id(), "Hello", null)
        );
        MessageDto message2 = messageService.create(
                new MessageCreateRequest(user2.id(), channel1.id(), "Hi", null)
        );

        System.out.println("===== Users =====");
        for (UserDto item : userService.findAll()) {
            System.out.println(item.username());
        }

        System.out.println("===== Channels =====");
        for (ChannelDto item : channelService.findAllByUserId(user1.id())) {
            System.out.println(item.name());
        }

        System.out.println("===== Messages =====");
        for (MessageDto item : messageService.findAllByChannelId(channel1.id())) {
            System.out.println(item.content());
        }

        System.out.println("===== 사용자 선택 조회 =====");
        userService.find(user1.id())
                .ifPresent(u -> System.out.println(u.username()));

        System.out.println("\n===== 사용자 전체 조회 =====");
        for (UserDto user : userService.findAll()) {
            System.out.println(user.username() + " : " + user.email());
        }

        System.out.println("\n===== 채널 수정 =====");
        channelService.update(
                new ChannelUpdateParam(
                        channel1.id(),
                        new ChannelUpdateRequest("Test2", "All chat2")
                )
        );
        channelService.find(channel1.id())
                .ifPresent(c -> System.out.println(c.name()));

        System.out.println("\n===== 메세지 수정 =====");
        messageService.update(
                new MessageUpdateParam(
                        message1.id(),
                        new MessageUpdateRequest("How are you?")
                )
        );
        messageService.find(message1.id())
                .ifPresent(m -> System.out.println(m.content()));

        System.out.println("\n===== 메세지 삭제 =====");
        messageService.delete(message2.id());
        if (messageService.find(message2.id()).isEmpty()) {
            System.out.println("메세지가 삭제되었습니다.");
        } else {
            System.out.println("메세지 삭제 실패");
        }
    }
}