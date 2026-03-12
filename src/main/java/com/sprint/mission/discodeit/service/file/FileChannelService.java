package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.InputMismatchException;
import java.util.List;
import java.util.UUID;

public class FileChannelService implements ChannelService {
    private ChannelRepository channelRepository;

    public FileChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    public Channel createChannel(String name, String description, ChannelType type) {
        Channel channel = new Channel(name, description, type);
        Channel savedChannel = channelRepository.save(channel);
        System.out.println(savedChannel.getName() + " 채널이 파일 시스템에 생성되었습니다.");
        return  savedChannel;
    }

    @Override
    public Channel readChannel(UUID id) {
        return channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 채널이 존재하지 않습니다."));
    }

    @Override
    public List<Channel> allReadChannel() {
        return channelRepository.findAll();
    }

    @Override
    public void deleteChannel(UUID id) {
        if(channelRepository.findById(id).isEmpty()) {
            throw  new IllegalArgumentException("패스워드가 일치하지 않습니다.");
        }

        channelRepository.delete(id);
    }

    @Override
    public void updateChannel(UUID id, String name, String description) {
        Channel channel = channelRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));
        channel.update(name, description);
        channelRepository.save(channel);
    }
}
