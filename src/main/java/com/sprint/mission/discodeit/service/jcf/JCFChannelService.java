//package com.sprint.mission.discodeit.service.jcf;
//
//import com.sprint.mission.discodeit.entity.Channel;
//import com.sprint.mission.discodeit.entity.ChannelType;
//import com.sprint.mission.discodeit.repository.ChannelRepository;
//import com.sprint.mission.discodeit.service.ChannelService;
//
//import java.util.InputMismatchException;
//import java.util.List;
//import java.util.UUID;
//
//public class JCFChannelService implements ChannelService {
//    private ChannelRepository channelRepository;
//
//    public JCFChannelService(ChannelRepository channelRepository) {
//        this.channelRepository = channelRepository;
//    }
//
//    @Override
//    public Channel createChannel(String name, String description, ChannelType type, UUID ownerId) {
//        Channel channel = new Channel(name, description, type, ownerId);
//        channelRepository.save(channel);
//        System.out.println(channel.getName() + " 채널이 생성되었습니다. 채널 생성시간 : " + channel.getCreatedAt());
//        return channel;
//    }
//
//    @Override
//    public Channel readChannel(UUID id) {
//        return channelRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("해당 채널이 존재하지 않습니다."));
//    }
//
//    @Override
//    public List<Channel> allReadChannel() {
//        return channelRepository.findAll();
//    }
//
//    @Override
//    public void deleteChannel(UUID id) {
//        Channel channel = channelRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));
//        if(channelRepository.findById(id).isEmpty()) {
//            throw  new IllegalArgumentException("삭제할 채널이 존재하지 않습니다.");
//        }
//
//        channelRepository.delete(id);
//    }
//
//    @Override
//    public void updateChannel(UUID id, String name, String description) {
//        Channel channel = channelRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));
//
//        channel.update(name, description);
//        channelRepository.save(channel);
//    }
//}
