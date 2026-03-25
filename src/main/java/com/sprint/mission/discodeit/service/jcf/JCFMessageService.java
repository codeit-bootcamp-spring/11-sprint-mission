//package com.sprint.mission.discodeit.service.jcf;
//
//import com.sprint.mission.discodeit.entity.Channel;
//import com.sprint.mission.discodeit.entity.Message;
//import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.service.MessageService;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class JCFMessageService implements MessageService{
//
//    private final List<Message> data;
//    private final JCFUserService userService;
//    private final JCFChannelService channelService;
//
//    public JCFMessageService(JCFUserService userService, JCFChannelService channelService) {
//        this.data = new ArrayList<>();
//        this.userService = userService;
//        this.channelService = channelService;
//    }
//
//    @Override
//    public Message create(Message message) {
//        getUserName(message.getUserId());
//        getChannelName(message.getChannelId());
//
//        data.add(message);
//        return message;
//    }
//
//    @Override
//    public List<String> getAllMessage() {
//        return data.stream()
//                .map(this::printMessage)
//                .toList();
//    }
//
//    @Override
//    public List<String> getMessageByChannel(UUID channelId) {
//        return data.stream()
//                .filter(m -> m.getChannelId().equals(channelId))
//                .map(this::printMessage)
//                .toList();
//
//    }
//
//    @Override
//    public Message findById(UUID id) {
//        return data.stream()
//                .filter(m -> m.getId().equals(id))
//                .findAny()
//                .orElseThrow(() -> new IllegalArgumentException("[message] 없는 id 입니다."));
//    }
//
//    @Override
//    public String getUserName(UUID userId) {
//        User user = userService.findById(userId);
//        return user.getName();
//    }
//
//    @Override
//    public String getChannelName(UUID channelId) {
//        Channel channel = channelService.findById(channelId);
//        return channel.getName();
//    }
//
//    @Override
//    public String updateContent(UUID id, String content) {
//        Message message = findById(id);
//        message.updateContent(content);
//        return printMessage(message);
//    }
//
//    @Override
//    public void delete(UUID id) {
//        Message message = findById(id);
//        data.remove(message);
//    }
//
//    @Override
//    public String printMessage(Message message) {
//        return "Message{" +
//                "userName='" + getUserName(message.getUserId()) + '\'' +
//                ", channelId='" + getChannelName(message.getChannelId()) + '\'' +
//                ", content='" + message.getContent() + '\'' +
//        "}";
//    }
//
//
//}
