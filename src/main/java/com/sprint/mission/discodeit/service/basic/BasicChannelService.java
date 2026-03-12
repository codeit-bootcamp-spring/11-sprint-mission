package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Domain.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;

    public BasicChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    public UUID create(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("채널이 null입니다.");
        }
        if (channel.getChannelName() == null) {
            throw new IllegalArgumentException("채널명이 null입니다.");
        }
        if (channel.getChannelName().isBlank()) {
            throw new IllegalArgumentException("채널명이 blank입니다.");
        }
        boolean isDuplicate = channelRepository.readAll().stream()
                .anyMatch(c -> c.getChannelName().equals(channel.getChannelName()));
        if (isDuplicate) {
            throw new IllegalArgumentException("이미 존재하는 채널명입니다.");
        }
        return channelRepository.create(channel);
    }

    @Override
    public Channel read(UUID id) {
        return channelRepository.read(id);
    }

    @Override
    public List<Channel> readAll() {
        return channelRepository.readAll();
    }

    @Override
    public void update(UUID id, String newName, String newDescription) {
        Channel channel = channelRepository.read(id);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        if (newName == null) {
            throw new IllegalArgumentException("채널명이 null입니다.");
        }
        if (newName.isBlank()) {
            throw new IllegalArgumentException("채널명이 blank입니다.");
        }
        boolean isDuplicate = channelRepository.readAll().stream()
                .filter(c -> !c.getId().equals(id))
                .anyMatch(c -> c.getChannelName().equals(newName));
        if (isDuplicate) {
            throw new IllegalArgumentException("이미 존재하는 채널명입니다.");
        }
        channel.updateChannel(newName, newDescription);
        channelRepository.create(channel);
    }

    @Override
    public void delete(UUID id) {
        Channel channel = channelRepository.read(id);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        channelRepository.delete(id);
    }
}