//package com.sprint.mission.discodeit.service.jcf;
//
//import com.sprint.mission.discodeit.entity.Channel;
//import com.sprint.mission.discodeit.entity.ChannelType;
//import com.sprint.mission.discodeit.service.ChannelService;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class JCFChannelService implements ChannelService {
//
//    private final Map<UUID, Channel> data;
//
//    public JCFChannelService() {
//        this.data = new HashMap<>();
//    }
//
//    @Override
//    public Channel create(Channel channel) {
//        data.put(channel.getId(), channel);
//        return channel;
//    }
//
//    @Override
//    public Map<String, List<Channel>> getAllChannel() {
//        return data.values().stream()
//                .collect(Collectors.groupingBy(Channel::getName));
//    }
//
//    @Override
//    public List<Channel> findByName(String name) {
//        return data.values().stream()
//                .filter(c -> c.getName().equals(name))
//                .toList();
//    }
//
//    @Override
//    public Channel findById(UUID id) {
//        Channel channel = data.get(id);
//        if (channel == null) {
//            throw new IllegalArgumentException("[channel] 없는 id 입니다.");
//        }
//        return channel;
//    }
//
//    @Override
//    public Channel updateName(UUID id, String name) {
//        Channel channel = findById(id);
//        channel.updateName(name);
//        return channel;
//    }
//
//    @Override
//    public Channel updateDescription(UUID id, String description) {
//        Channel channel = findById(id);
//        channel.updateDescription(description);
//        return channel;
//    }
//
//    @Override
//    public Channel updateType(UUID id, ChannelType type) {
//        Channel channel = findById(id);
//        channel.updateType(type);
//        return channel;
//    }
//
//    @Override
//    public void delete(UUID id) {
//        findById(id);
//        data.remove(id);
//    }
//}
