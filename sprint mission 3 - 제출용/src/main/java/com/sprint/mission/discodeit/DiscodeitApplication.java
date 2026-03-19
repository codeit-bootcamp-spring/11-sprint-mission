package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.service.*;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Instant;
import java.util.List;

@SpringBootApplication
public class DiscodeitApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

        UserService userService = context.getBean(UserService.class);
        ChannelService channelService = context.getBean(ChannelService.class);
        MessageService messageService = context.getBean(MessageService.class);
        AuthService authService = context.getBean(AuthService.class);
        ReadStatusService readStatusService = context.getBean(ReadStatusService.class);
        UserStatusService userStatusService = context.getBean(UserStatusService.class);
        BinaryContentService binaryContentService = context.getBean(BinaryContentService.class);

        // ===== User 테스트 =====
        System.out.println("\n===== User 테스트 =====");
        UserResponse user1 = userService.create(new UserCreateRequest("정수용", "wjdtndyd0001@gmail.com", "1234", null, null, null));
        System.out.println("유저 생성: " + user1.getUserName());

        UserResponse foundUser = userService.read(user1.getId());
        System.out.println("유저 조회: " + foundUser.getUserName() + " | 온라인: " + foundUser.isOnline());

        userService.update(new UserUpdateRequest(user1.getId(), "정수용수정", "wjdtndyd0002@gmail.com", "5678", null, null, null));
        System.out.println("유저 수정: " + userService.read(user1.getId()).getUserName());

        // ===== Auth 테스트 =====
        System.out.println("\n===== Auth 테스트 =====");
        try {
            authService.login(new LoginRequest("정수용수정", "틀린비밀번호"));
        } catch (IllegalArgumentException e) {
            System.out.println("로그인 실패: " + e.getMessage());
        }
        UserResponse loginUser = authService.login(new LoginRequest("정수용수정", "5678"));
        System.out.println("로그인 성공: " + loginUser.getUserName());

        // ===== UserStatus 테스트 =====
        System.out.println("\n===== UserStatus 테스트 =====");
        userStatusService.readAll().forEach(us ->
                System.out.println("userId: " + us.getUserId() + " | 온라인: " + us.isOnline()));
        userStatusService.update(new UserStatusUpdateRequest(user1.getId(), Instant.now()));
        System.out.println("UserStatus 업데이트 완료 후 At: "+ userStatusService.read(user1.getId()).getLastOnlineAt());   // user는 생성시 자동으로 온라인상태 포함

        // ===== Channel 테스트 =====
        System.out.println("\n===== Channel 테스트 =====");
        ChannelResponse publicChannel = channelService.createPublicChannel(new PublicChannelCreateRequest("PUBLIC채널", "PUBLIC채널"));
        System.out.println("PUBLIC 채널 생성: " + publicChannel.getChannelName());

        ChannelResponse privateChannel = channelService.createPrivateChannel(new PrivateChannelCreateRequest(List.of(user1.getId())));
        System.out.println("PRIVATE 채널 생성 완료"); // ReadStatus 자동 생성 포함

        channelService.update(new ChannelUpdateRequest(publicChannel.getChannelId(), "수정된PUBLIC채널", "수정된PUBLIC채널"));
        System.out.println("채널 수정: " + channelService.read(publicChannel.getChannelId()).getChannelName());

        // ===== readAllByUserId 테스트 =====
        System.out.println("\n===== readAllByUserId 테스트 =====");
        channelService.readAllByUserId(user1.getId()).forEach(c ->
                System.out.println("채널: " + (c.getChannelName() != null ? c.getChannelName() : "PRIVATE 채널")));

        // ===== ReadStatus 테스트 =====
        System.out.println("\n===== ReadStatus 테스트 =====");
        ReadStatus readStatus = readStatusService.create(new ReadStatusCreateRequest(user1.getId(), publicChannel.getChannelId()));
        System.out.println("ReadStatus 생성 완료");

        readStatusService.readAllByUserId(user1.getId()).forEach(rs ->
                System.out.println("channelId: " + rs.getChannelId() + " | lastReadAt: " + rs.getLastMessageAt()));

        readStatusService.update(new ReadStatusUpdateRequest(user1.getId(), publicChannel.getChannelId(), Instant.now()));
        System.out.println("ReadStatus 업데이트 완료");

        // ===== Message 테스트 =====
        System.out.println("\n===== Message 테스트 =====");
        Message message = messageService.create(new MessageCreateRequest("안녕하세요!", user1.getId(), publicChannel.getChannelId(), null, null, null, null));
        System.out.println("메시지 생성: " + message.getMessageContent());

        messageService.update(new MessageUpdateRequest(message.getId(), "수정된 메시지"));
        System.out.println("메시지 수정: " + messageService.read(message.getId()).getMessageContent());

        // ===== BinaryContent 테스트 =====
        System.out.println("\n===== BinaryContent 테스트 =====");
        BinaryContent binaryContent = binaryContentService.create(new BinaryContentCreateRequest(user1.getId(), null, "profile.png", new byte[]{1, 2, 3}, "image/png"));
        System.out.println("BinaryContent 생성: " + binaryContent.getFileName());

        binaryContentService.read(binaryContent.getId());
        System.out.println("BinaryContent 조회 완료");

        binaryContentService.delete(binaryContent.getId());
        System.out.println("BinaryContent 삭제 완료");

        // ===== 삭제 테스트 후 조회 =====
        System.out.println("\n===== 삭제 테스트 =====");
        messageService.delete(message.getId());
        System.out.println("메시지 삭제 완료");
        try {
                messageService.read(message.getId());
        } catch (DiscodeitException e) {
            System.out.println("메시지 삭제 확인: " + e.getMessage());
        }

        channelService.delete(publicChannel.getChannelId());
        System.out.println("채널 삭제 완료");
        try {
            channelService.read(publicChannel.getChannelId());
        } catch (DiscodeitException  e) {
            System.out.println("채널 삭제 확인: " + e.getMessage());
        }

        userService.delete(user1.getId());
        System.out.println("유저 삭제 완료");
        try {
            userService.read(user1.getId());
        } catch (DiscodeitException  e) {
            System.out.println("유저 삭제 확인: " + e.getMessage());
        }

        // ===== Restore 테스트 =====
        System.out.println("\n===== Restore 테스트 =====");
        userService.restore(user1.getId());
        System.out.println("유저 복구 완료: " + userService.read(user1.getId()).getUserName());

        channelService.restore(publicChannel.getChannelId());
        System.out.println("채널 복구 완료: " + channelService.read(publicChannel.getChannelId()).getChannelName());

    }
}