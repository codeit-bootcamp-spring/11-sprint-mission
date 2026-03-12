package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepo;

    public Channel create(ChannelType channelType, String name, String description) {
        Channel channel = new Channel(channelType, name, description);
        channelRepo.save(channel);
        return channel;
    }

    public Channel findById(UUID id) {
        return channelRepo.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));
    }

    public List<Channel> findAll() {
        return channelRepo.findAll();
    }

    public void update(Channel oldChannel, Channel newChannel) {
        oldChannel.setChannelType(newChannel.getChannelType());
        oldChannel.setName(newChannel.getName());
        oldChannel.setDescription(newChannel.getDescription());
        oldChannel.update();
        channelRepo.save(oldChannel);
    }

    public void delete(Channel channel) {
        channelRepo.delete(channel);
    }
}
