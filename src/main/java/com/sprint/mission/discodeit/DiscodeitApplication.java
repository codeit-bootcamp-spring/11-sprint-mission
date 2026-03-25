package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
@Slf4j
public class DiscodeitApplication {
    static User setupUser(UserService userService, UserCreateDto dto) {
        User user = userService.create(dto); // 유저 이름, 유저 이메일, 유저 비밀번호, attachment(프로필이미지) 파일(바이트), attachment(프로필이미지) 이름, 파일타입
        return user;
    }

    static Channel setupPublicChannel(ChannelService channelService, ChannelCreatePublicDto dto) {
        Channel channel = channelService.createPublic(dto); // 채널 이름, 채널 설명
        return channel;
    }

    static Channel setupPrivateChannel(ChannelService channelService, List<UUID> participantIds) {
        ChannelCreatePrivateDto dto = new ChannelCreatePrivateDto(participantIds);
        Channel channel = channelService.createPrivate(dto); // 속해있는 유저들
        return channel;
    }

    static Message messageCreateTest(MessageService messageService, MessageCreateDto dto) {
        Message message = messageService.create(dto); // 메시지 내용, 채널 id, 작성자(유저) id
        log.info("메시지를 생성하였습니다 : {}", message.getId());
        return message;
    }

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

        // 셋업(유저)
        User user1 = setupUser(userService, new UserCreateDto(
                "woody",
                "woody@codit.com",
                "woody1234",
                null, null, null)
        );

        User user2 = setupUser(userService, new UserCreateDto(
                "홍길동",
                "hong123@gmail.com",
                "guildong1", null, null, null)
        );

        // 유저 프로필 이미지와 같이 유저를 셋업
        User user3 = setupUser(userService, new UserCreateDto(
                "김명근",
                "rlaaudrms369@naver.com",
                "password",
                Files.readAllBytes(Path.of("attachments/userProfile/user3.png")),
                "user2",
                "image/png")
        );


        // 셋업(채널)
        Channel publicChannel = setupPublicChannel(channelService, new ChannelCreatePublicDto("공지", "공지 채널입니다."));
        Channel privateChannel = setupPrivateChannel(channelService, List.of(user2.getId(), user3.getId()));


        // 셋업(메시지)
        Message message1 = messageCreateTest(messageService, new MessageCreateDto(
                "안녕하세요.",
                publicChannel.getId(),
                user1.getId(), null)
        );

        Message message2 = messageCreateTest(messageService, new MessageCreateDto(
                "반갑습니다.",
                privateChannel.getId(),
                user2.getId(), null)
        );

        // 메시지와 첨부파일을 동시에 셋업
        Message message3 = messageCreateTest(messageService, new MessageCreateDto(
                "test.png가 담긴 테스트 메시지입니다.",
                privateChannel.getId(),
                user3.getId(),
                List.of(
                        new BinaryContentCreateDto(
                                null,
                                null,
                                Files.readAllBytes(Path.of("attachments/message/test1.png")),
                                "test",
                                "image/png"
                        )
                )
        ));

        // 조회(단건)
        log.info("========== 단건 조회하기 ==========");

        // User
        log.info("========== user1 ==========");
        log.info("{}", userService.find(user1.getId()));

        log.info("========== user2 ==========");
        log.info("{}", userService.find(user2.getId()));

        log.info("========== user3 ==========");
        log.info("{}", userService.find(user3.getId()));

        // Channel
        log.info("========== publicChannel ==========");
        log.info("{}", channelService.find(publicChannel.getId()));
        log.info("========== privateChannel ==========");
        log.info("{}", channelService.find(privateChannel.getId()));

        // BinaryContent(이름만) : user3 - profile 이미지 / message - 첨부파일(이미지)
        log.info("========== user3의 profileImage ==========");
        binaryContentService.findAllByIdIn(List.of(user3.getProfileId()))
                .forEach(binaryContent -> log.info("파일 이름 : {}, 파일 형식 : {}",
                        binaryContent.getFileName(), binaryContent.getFileType()
                ));

        log.info("========== message3의 첨부파일(이미지) ==========");
        binaryContentService.findAllByIdIn(message3.getAttachmentIds())
                .forEach(binaryContent -> log.info("파일 이름 : {}, 파일 형식 : {}",
                        binaryContent.getFileName(), binaryContent.getFileType()
                ));


        // 조회(다건)
        log.info("========== 다건 조회하기 ==========");

        log.info("========== 모든 유저 목록(이름만) ==========");
        userService.findAll().forEach(user -> log.info(user.name()));

        log.info("========== user1이 속한 채널(이름만) 목록 ==========");
        channelService.findAllByUserId(user1.getId()).forEach(channel -> log.info(channel.name()));

        log.info("========== user2가 속한 채널(이름만) 목록 ==========");
        channelService.findAllByUserId(user2.getId()).forEach(channel -> log.info(channel.name()));

        log.info("========== user3이 속한 채널(이름만) 목록 ==========");
        channelService.findAllByUserId(user3.getId()).forEach(channel -> log.info(channel.name()));

        log.info("========== publicChannel(공지)이 속한 메시지 목록(내용만) ==========");
        messageService.findAllByChannelId(publicChannel.getId()).forEach(message -> log.info(message.getContent()));

        log.info("========== privateChannel(공지)이 속한 메시지 목록(내용만) ==========");
        messageService.findAllByChannelId(privateChannel.getId()).forEach(message -> log.info(message.getContent()));

        log.info("========== user1이 속한 채널들의 마지막으로 읽은 메시지 시간 ==========");
        readStatusService.findAllByUserId(user1.getId()).forEach(readStatus -> log.info("{}", readStatus.getLastReadAt()));

        log.info("========== user2가 속한 채널들의 마지막으로 읽은 메시지 시간 ==========");
        readStatusService.findAllByUserId(user2.getId()).forEach(readStatus -> log.info("{}", readStatus.getLastReadAt()));

        log.info("========== user3이 속한 채널들의 마지막으로 읽은 메시지 시간 ==========");
        readStatusService.findAllByUserId(user3.getId()).forEach(readStatus -> log.info("{}", readStatus.getLastReadAt()));

        log.info("========== 모든 유저의 마지막 접속시간, 온/오프라인 상태 ==========");
        userStatusService.findAll().forEach(userStatus -> log.info("유저 이름 : {}, 유저 상태 : {}, 유저 최근 접속시간 : {}",
                userService.find(userStatus.getUserId()).name(),
                userStatus.isStatus(),
                userStatus.getLastOnlineAt()
        ));


        // 삭제
        log.info("========== 메시지, 유저, 채널, UserStatus, ReadStatus, BinaryContent 순으로 삭제 ==========");
        messageService.delete(message1.getId());
        messageService.delete(message2.getId());
        messageService.delete(message3.getId());

        userService.delete(user1.getId());
        userService.delete(user2.getId());
        userService.delete(user3.getId());

        channelService.delete(publicChannel.getId());
        channelService.delete(privateChannel.getId());

        userStatusService.delete(user1.getId());
        userStatusService.delete(user2.getId());
        userStatusService.delete(user3.getId());

        readStatusService.delete(user1.getId());
        readStatusService.delete(user2.getId());
        readStatusService.delete(user3.getId());

        binaryContentService.delete(user3.getId());
        binaryContentService.delete(message3.getId());

        // 삭제 확인
        log.info("========== 정상적으로 삭제되었는지 조회하여 확인 ==========");
        userService.findAll().forEach(userReadDto -> log.info("{}", userReadDto));

        channelService.findAllByUserId(user1.getId()).forEach(channelReadDto -> log.info("{}", channelReadDto));
        channelService.findAllByUserId(user2.getId()).forEach(channelReadDto -> log.info("{}", channelReadDto));
        channelService.findAllByUserId(user3.getId()).forEach(channelReadDto -> log.info("{}", channelReadDto));

        messageService.findAllByChannelId(publicChannel.getId()).forEach(message -> log.info("{}", message));
        messageService.findAllByChannelId(privateChannel.getId()).forEach(message -> log.info("{}", message));
    }
}