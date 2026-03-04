package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.jcf.*;

import java.util.List;
import java.util.UUID;

public class JavaApplication {
    public static void main(String[] args) {

        // 1. 서비스들 생성 (순서 중요!)
        JCFUserService userService = new JCFUserService();
        JCFChannelService channelService = new JCFChannelService();
        JCFMessageService messageService = new JCFMessageService(userService, channelService);

        // 2. 등록 테스트
        System.out.println("=== 1. 등록 ===");
        User user = userService.create(new User("warning"));
        Channel channel = channelService.create(new Channel("general"));
        System.out.println("✓ User 등록: " + user.getName());
        System.out.println("✓ Channel 등록: " + channel.getName());

        // 3. 메시지 등록 (검증 테스트)
        System.out.println("\n=== 2. 메시지 등록 ===");
        Message message = messageService.create(
                new Message(user.getId(), channel.getId(), "Hello Discoed!")
        );
        System.out.println("✓ Message 등록: " + message.getContent());

        // 4. 조회 테스트
        System.out.println("\n=== 3. 조회 ===");
        System.out.println("User 전체 수: " + userService.getAll().size());
        System.out.println("Message 내용: " + messageService.getById(message.getId()).getContent());

        // 5. 삭제 테스트
        System.out.println("\n=== 4. 삭제 ===");
        messageService.delete(message.getId());
        System.out.println("삭제 후 Message: " + messageService.getById(message.getId()));

        System.out.println("\n🎉 모든 테스트 완료!");
    }
}
