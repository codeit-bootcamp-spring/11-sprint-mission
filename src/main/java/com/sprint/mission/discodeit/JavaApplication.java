//package com.sprint.mission.discodeit;
//
//import com.sprint.mission.discodeit.entity.Channel;
//import com.sprint.mission.discodeit.entity.ChannelType;
//import com.sprint.mission.discodeit.entity.Message;
//import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.repository.UserRepository;
//import com.sprint.mission.discodeit.repository.jcf.JCFChannelRepository;
//import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
//import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
//import com.sprint.mission.discodeit.service.ChannelService;
//import com.sprint.mission.discodeit.service.MessageService;
//import com.sprint.mission.discodeit.service.UserService;
//import com.sprint.mission.discodeit.service.basic.BasicChannelService;
//import com.sprint.mission.discodeit.service.basic.BasicMessageService;
//import com.sprint.mission.discodeit.service.basic.BasicUserService;
//
//public class JavaApplication {
//    static User setupUser(UserService userService) {
//        User user = userService.create("woody", "woody@codeit.com", "woody1234");
//        return user;
//    }
//
//    static Channel setupChannel(ChannelService channelService) {
//        Channel channel = channelService.create(ChannelType.PUBLIC, "공지", "공지 채널입니다.");
//        return channel;
//    }
//
//    static void messageCreateTest(MessageService messageService, Channel channel, User user) {
//        Message message = messageService.create("안녕하세요.", user.getId(), channel.getId());
//        System.out.println("메시지 생성 완료: " + message.getId());
//    }
//
//    public static void main(String[] args) {
//        UserRepository userRepository = new JCFUserRepository();
//        UserService userService = new BasicUserService(userRepository);
//
//        JCFChannelRepository channelRepository = new JCFChannelRepository();
//        ChannelService channelService = new BasicChannelService(channelRepository);
//
//        JCFMessageRepository messageRepository = new JCFMessageRepository();
//        MessageService messageService = new BasicMessageService(messageRepository, userRepository, channelRepository);
//
//        User user = setupUser(userService);
//        Channel channel = setupChannel(channelService);
//        messageCreateTest(messageService, channel, user);
//    }
//}
