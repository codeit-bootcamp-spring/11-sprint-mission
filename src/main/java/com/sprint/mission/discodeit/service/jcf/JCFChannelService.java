//package com.sprint.mission.discodeit.service.jcf;
//
//import com.sprint.mission.discodeit.entity.Channel;
//import com.sprint.mission.discodeit.entity.ChannelType;
//import com.sprint.mission.discodeit.service.ChannelService;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//public class JCFChannelService implements ChannelService {
//    private final Map<UUID, Channel> data;
//
//    public JCFChannelService() {
//        this.data = new HashMap<>();
//    }
//
//    @Override
//    public Channel create(ChannelType type, String name, String description) {
//        Channel channel = new Channel(type, name, description);
//        data.put(channel.getId(), channel);
//        return channel;
//    }
//
//    @Override
//    public Channel findById(UUID id) {
//        return data.get(id);
//    }
//
//    @Override
//    public List<Channel> findAll() {
//        return data.values().stream().toList();
//    }
//
//    @Override
//    public void update(UUID id, String name) {
//        Channel channel = data.get(id);
//        if (channel != null) {
//            channel.update(name);
//        }
//    }
//
//    @Override
//    public void delete(UUID id) {
//        data.remove(id);
//    }
//}
