package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequestDto;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequestDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepo;
    private final ReadStatusRepository readStatusRepo;

    public Channel createPublicChannel(PublicChannelCreateRequestDto dto) {
        Channel channel = new Channel(ChannelType.PUBLIC, dto.name(), dto.description());
        channelRepo.save(channel);
        return channel;
    }

    public Channel createPrivateChannel(PrivateChannelCreateRequestDto dto) {
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        channelRepo.save(channel);

        for(UUID userId : dto.userIdList()) {
            readStatusRepo.save(new ReadStatus(userId, channel.getId()));
        }

        return channel;
    }

    public Channel findById(UUID id) {
        return channelRepo.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));
    }

    public List<Channel> findAll() {
        return channelRepo.findAll();
    }

    public void update(UUID id, Channel newChannel) {
        Channel oldChannel = findById(id);

        oldChannel.setChannelType(newChannel.getChannelType());
        oldChannel.setName(newChannel.getName());
        oldChannel.setDescription(newChannel.getDescription());
        oldChannel.setAttachmentIds(newChannel.getAttachmentIds());
        oldChannel.update();

        channelRepo.save(oldChannel);
    }

    public void delete(Channel channel) {
        channelRepo.delete(channel);
    }
}
