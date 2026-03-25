//package com.sprint.mission.discodeit.service.jcf;
//
//import com.sprint.mission.discodeit.entity.Message;
//import com.sprint.mission.discodeit.service.ChannelService;
//import com.sprint.mission.discodeit.service.MessageService;
//import com.sprint.mission.discodeit.service.UserService;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//public class JCFMessageService implements MessageService {
//    private final Map<UUID, Message> data;
//    private final UserService userService;
//    private final ChannelService channelService;
//
//    public JCFMessageService(UserService userService, ChannelService channelService) {
//        this.data = new HashMap<>();
//        this.userService = userService;
//        this.channelService = channelService;
//    }
//
//    @Override
//    public Message create(String name, UUID userId, UUID channelId) {
//        if (userService.findById(userId) == null) {
//            throw new IllegalArgumentException("유저가 존재하지 않습니다.");
//        }
//        if (channelService.findById(channelId) == null) {
//            throw new IllegalArgumentException("채널이 존재하지 않습니다.");
//        }
//        Message message = new Message(name, userId, channelId);
//        data.put(message.getId(), message);
//        return message;
//    }
//
//    @Override
//    public Message findById(UUID id) {
//        return data.get(id);
//    }
//
//    @Override
//    public List<Message> findAll() {
//        return data.values().stream().toList();
//    }
//
//    @Override
//    public void update(UUID id, String name) {
//        Message message = data.get(id);
//        if(message != null) {
//            message.update(name);
//        }
//    }
//
//    @Override
//    public void delete(UUID id) {
//        data.remove(id);
//    }
//}
