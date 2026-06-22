package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFChannelRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;

import java.util.List;

public class JavaApplication {

    static void userCRUDTest(UserService userService) {
        System.out.println("=== User 테스트 ===");
        User user = userService.create("woody", "woody@codeit.com", "woody1234");
        System.out.println("생성: " + user.getUsername() + " / " + user.getEmail());

        User foundUser = userService.find(user.getId());
        System.out.println("단건 조회: " + foundUser.getUsername());

        List<User> users = userService.findAll();
        System.out.println("다건 조회: " + users.size() + "명");

        userService.update(user.getId(), "woody2", null, null);
        System.out.println("수정 후 조회: " + userService.find(user.getId()).getUsername());

        userService.delete(user.getId());
        System.out.println("삭제 후 개수: " + userService.findAll().size() + "명");
    }

    static void channelCRUDTest(ChannelService channelService) {
        System.out.println("\n=== Channel 테스트 ===");
        Channel channel = channelService.create(ChannelType.PUBLIC, "공지", "공지 채널입니다.");
        System.out.println("생성: " + channel.getName());

        Channel foundChannel = channelService.find(channel.getId());
        System.out.println("단건 조회: " + foundChannel.getName());

        List<Channel> channels = channelService.findAll();
        System.out.println("다건 조회: " + channels.size() + "개");

        channelService.update(channel.getId(), "자유", "자유 채널입니다.");
        System.out.println("수정 후 조회: " + channelService.find(channel.getId()).getName());

        channelService.delete(channel.getId());
        System.out.println("삭제 후 개수: " + channelService.findAll().size() + "개");
    }

    static void messageCRUDTest(MessageService messageService, UserService userService, ChannelService channelService) {
        System.out.println("\n=== Message 테스트 ===");
        User user = userService.create("woody", "woody@codeit.com", "woody1234");
        Channel channel = channelService.create(ChannelType.PUBLIC, "공지", "공지 채널입니다.");

        Message message = messageService.create("안녕하세요.", channel.getId(), user.getId());
        System.out.println("생성: " + message.getContent());

        Message foundMessage = messageService.find(message.getId());
        System.out.println("단건 조회: " + foundMessage.getContent());

        List<Message> messages = messageService.findAll();
        System.out.println("다건 조회: " + messages.size() + "개");

        messageService.update(message.getId(), "반갑습니다.");
        System.out.println("수정 후 조회: " + messageService.find(message.getId()).getContent());

        messageService.delete(message.getId());
        System.out.println("삭제 후 개수: " + messageService.findAll().size() + "개");
    }

    static User setupUser(UserService userService) {
        User user = userService.create("woody", "woody@codeit.com", "woody1234");
        return user;
    }

    static Channel setupChannel(ChannelService channelService) {
        Channel channel = channelService.create(ChannelType.PUBLIC, "공지", "공지 채널입니다.");
        return channel;
    }

    static void messageCreateTest(MessageService messageService, Channel channel, User author) {
        Message message = messageService.create("안녕하세요.", channel.getId(), author.getId());
        System.out.println("메시지 생성: " + message.getId());
    }

    public static void main(String[] args) {
        // JCF Repository 사용
//        UserRepository userRepository = new JCFUserRepository();
//        ChannelRepository channelRepository = new JCFChannelRepository();
//        MessageRepository messageRepository = new JCFMessageRepository();

        // File Repository 사용
        UserRepository userRepository = new FileUserRepository();
        ChannelRepository channelRepository = new FileChannelRepository();
        MessageRepository messageRepository = new FileMessageRepository();

        UserService userService = new BasicUserService(userRepository);
        ChannelService channelService = new BasicChannelService(channelRepository);
        MessageService messageService = new BasicMessageService(messageRepository, channelRepository, userRepository);

        User user = setupUser(userService);
        Channel channel = setupChannel(channelService);
        messageCreateTest(messageService, channel, user);
    }
}
