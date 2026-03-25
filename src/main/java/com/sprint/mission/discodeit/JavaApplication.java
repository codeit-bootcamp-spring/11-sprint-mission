package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.repository.jcf.*;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;


/*public class JavaApplication {
    static User setupUser(UserService userService) {
        User user = userService.create(new User("woody", "온라인"));
        return user;
    }

    static Channel setupChannel(ChannelService channelService) {
        Channel channel = channelService.create(new Channel("공지", "공지 채널입니다.", "채팅방"));
        return channel;
    }

    static void messageCreateTest(MessageService messageService, User author, Channel channel) {
        Message message = messageService.create(new Message(author.getId(), channel.getId(), "안녕하세요."));
        System.out.println("메시지 생성: " + message.getId());
    }

    public static void main(String[] args) {
        // 서비스 초기화
        // TODO Basic*Service 구현체를 초기화하세요.

//        UserRepository userRepository = new JCFUserRepository();
//        ChannelRepository channelRepository = new JCFChannelRepository();
//        MessageRepository messageRepository = new JCFMessageRepository();
//
        UserRepository userRepository = new FileUserRepository();
        ChannelRepository channelRepository = new FileChannelRepository();
        MessageRepository messageRepository = new FileMessageRepository();

        UserService userService = new BasicUserService(userRepository);
        ChannelService channelService = new BasicChannelService(channelRepository);
        MessageService messageService = new BasicMessageService(messageRepository, userRepository, channelRepository);


        // 셋업
        User user = setupUser(userService);
        Channel channel = setupChannel(channelService);
        // 테스트
        messageCreateTest(messageService, user, channel);
    }
}*/

public class JavaApplication {

//    static void userTest(UserService userService) {
//        System.out.println("-------------------- 1. User 서비스 테스트 --------------------");
//
//        System.out.println();
//        System.out.println("=== 새로운 유저가 있습니다. ===");
//        User u1 = userService.create(new User("피카츄", "온라인"));
//        User u2 = userService.create(new User("이브이", "온라인"));
//        User u3 = userService.create(new User("파이리", "온라인"));
//
//        System.out.println("새로운 유저: " + u1);
//        System.out.println("새로운 유저: " + u2);
//        System.out.println("새로운 유저: " + u3);
//
//        System.out.println();
//        System.out.println("=== 전체 사용자 목록을 출력합니다. ===");
//        System.out.println(userService.getAllUser());
//
//        System.out.println();
//        System.out.println("=== [" + u1.getName() + "] 님을 조회합니다. ===");
//        System.out.println(userService.findById(u1.getId()));
//
//        System.out.println();
//        System.out.println("=== [" + u1.getName() + "] 님이 이름을 변경했습니다. ===");
//        System.out.println(userService.updateName(u1.getId(), "라이츄"));
//
//        System.out.println();
//        System.out.println("=== [" + userService.findById(u1.getId()).getName() + "] 님이 상태를 변경했습니다. ===");
//        System.out.println(userService.updateStatus(u1.getId(), "오프라인"));
//
//        System.out.println();
//        System.out.println("=== [" + u3.getName() + "]님이 탈퇴를 요청했습니다. ===");
//        userService.delete(u3.getId());
//
//        System.out.println();
//        System.out.println("=== 전체 사용자 목록을 출력합니다. ===");
//        System.out.println(userService.getAllUser());
//    }
//
//    static void channelTest(ChannelService channelService) {
//        System.out.println("-------------------- 2. Channel 서비스 테스트 --------------------");
//
//        System.out.println();
//        System.out.println("=== 새로운 채널이 생성되었습니다. ===");
//        Channel c1 = channelService.create(new Channel("행정-공지방", "행정 관련 공지를 올리는 방입니다.", ChannelType.TEXT));
//        Channel c2 = channelService.create(new Channel("음식-소통방", "주변의 맛집을 추천하는 방입니다.", ChannelType.VOICE));
//        Channel c3 = channelService.create(new Channel("이벤트-공지방", "이벤트 공지를 방입니다.", ChannelType.TEXT));
//
//        System.out.println(c1);
//        System.out.println(c2);
//        System.out.println(c3);
//
//        System.out.println();
//        System.out.println("=== 채널명별로 모든 채널을 출력합니다. ===");
//        System.out.println(channelService.getAllChannel());
//
//        System.out.println();
//        System.out.println("=== [" + c1.getName() + "]을 조회합니다. ===");
//        System.out.println(channelService.findByName(c1.getName()));
//
//        System.out.println();
//        System.out.println("=== [" + c1.getName() + "]의 이름을 변경합니다. ===");
//        System.out.println(channelService.updateName(c1.getId(), "학습-공지방"));
//
//        System.out.println();
//        System.out.println("=== [" + channelService.findById(c1.getId()).getName() + "]의 설명을 변경합니다. ===");
//        System.out.println(channelService.updateDescription(c1.getId(), "학습 관련 공지를 올리는 방입니다."));
//
//        System.out.println();
//        System.out.println("=== [" + channelService.findById(c1.getId()).getName() + "]의 타입을 변경합니다. ===");
//        System.out.println(channelService.updateType(c1.getId(), ChannelType.VOICE));
//
//        System.out.println();
//        System.out.println("=== [" + channelService.findById(c1.getId()).getName() + "]을 삭제합니다. ===");
//        channelService.delete(c1.getId());
//
//        System.out.println();
//        System.out.println("=== 삭제를 확인하기 위해 모든 채널을 출력합니다. ===");
//        System.out.println(channelService.getAllChannel());
//    }
//
//    static void messageTest(MessageService messageService, User user, Channel channel) {
//
//        System.out.println("-------------------- 3. Message 서비스 테스트 --------------------");
//
//        System.out.println();
//        System.out.println("=== 새로운 메시지가 생성되었습니다. ===");
//
//        Message m1 = messageService.create(new Message(user.getId(), channel.getId(), "안녕하세요!"));
////        Message m2 = messageService.create(new Message(u2.getId(), c2.getId(), "안녕 안녕"));
////        Message m3 = messageService.create(new Message(u1.getId(), c3.getId(), "이곳은 이벤트 공지방입니다."));
//
//        System.out.println(messageService.printMessage(m1));
////        System.out.println(messageService.printMessage(m2));
////        System.out.println(messageService.printMessage(m3));
//
//        System.out.println();
//        System.out.println("=== 모든 메시지를 출력합니다. ===");
//        System.out.println(messageService.getAllMessage());
//
//        System.out.println();
//        System.out.println("=== [" + messageService.getChannelName(m1.getChannelId()) + "] 의 모든 메시지를 출력합니다. ===");
//        System.out.println(messageService.getMessageByChannel(m1.getChannelId()));
//
//        System.out.println();
//        System.out.println("=== [" + messageService.getUserName(m1.getUserId()) + "] 님이 메시지 수정을 요청했습니다. ===");
//        System.out.println(messageService.updateContent(m1.getId(), "반갑습니다!"));
//
//        System.out.println();
//        System.out.println("=== 작성자 [" + messageService.getUserName(m1.getUserId()) + "]님의 \"" + m1.getContent() + "\" 메시지를 삭제합니다. ===");
//        messageService.delete(m1.getId());
//
//        System.out.println();
//        System.out.println("=== 삭제를 확인하기 위해 모든 메시지를 출력합니다. ===");
//        System.out.println(messageService.getAllMessage());
//    }
//
//    static User setupUser(UserService userService) {
//        User user = userService.create(new User("주리비얀", "온라인"));
//        return user;
//    }
//
//    static Channel setupChannel(ChannelService channelService) {
//        Channel channel = channelService.create(new Channel("음식-소통방", "주변의 맛집을 추천하는 방입니다.", ChannelType.VOICE));
//        return channel;
//    }

//    public static void main(String[] args) {
//        UserRepository userRepository = new JCFUserRepository();
//        ChannelRepository channelRepository = new JCFChannelRepository();
//        MessageRepository messageRepository = new JCFMessageRepository();
//
//        ReadStatusRepository readStatusRepository = new JCFReadStatusRepository();
//        UserStatusRepository userStatusRepository = new JCFUserStatusRepository();
//        BinaryContentRepository binaryContentRepository = new JCFBinaryContentRepository();
//
//        UserService userService = new BasicUserService(userRepository, binaryContentRepository, userStatusRepository);
//        ChannelService channelService = new BasicChannelService(channelRepository, userRepository, messageRepository, readStatusRepository);
//        MessageService messageService = new BasicMessageService(messageRepository, userRepository, channelRepository, binaryContentRepository);
//    }
}
