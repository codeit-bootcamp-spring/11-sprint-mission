package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.InputMismatchException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;

    @Override
    public Channel createChannel(String name, String description, ChannelType type, UUID ownerId) {
        Channel channel = new Channel(name, description, type, ownerId);
        Channel savedChannel = channelRepository.save(channel);
        System.out.println(savedChannel.getName() + " 채널이 생성되었습니다.");
        return channel;
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
        if(!channelRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("삭제할 채널이 존재하지 않습니다.");
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
