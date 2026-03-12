package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;

    @Override
    public Channel createChannel(String name, int capacity, User owner) {
        Channel channel = new Channel(name, capacity, owner);
        channelRepository.save(channel);
        owner.getOwnedChannels().add(channel);
        owner.getJoinedChannels().add(channel);
        return channel;
    }

    @Override
    public Channel getChannelById(UUID id) {
        return channelRepository.findById(id);
    }

    @Override
    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    @Override
    public void updateChannel(UUID id, String name, int capacity) {
        Channel channel = channelRepository.findById(id);
        if (channel != null) {
            channel.update(name, capacity);
            channelRepository.save(channel);
        }
    }

    @Override
    public boolean joinChannel(UUID channelId, User user) {
        Channel channel = channelRepository.findById(channelId);
        if (channel == null) {
            return false;
        }
        boolean success = channel.addParticipant(user);
        if (success) {
            user.getJoinedChannels().add(channel);
            channelRepository.save(channel);
        }
        return success;
    }

    @Override
    public boolean leaveChannel(UUID channelId, User user) {
        Channel channel = channelRepository.findById(channelId);
        if (channel == null) {
            return false;
        }
        boolean success = channel.removeParticipant(user);
        if (success) {
            user.getJoinedChannels().remove(channel);
            channelRepository.save(channel);
        }
        return success;
    }

    @Override
    public boolean kickUser(UUID channelId, User owner, User targetUser) {
        Channel channel = channelRepository.findById(channelId);
        if (channel == null || !channel.getOwner().equals(owner)) {
            return false;
        }
        boolean success = channel.removeParticipant(targetUser);
        if (success) {
            targetUser.getJoinedChannels().remove(channel);
            channelRepository.save(channel);
        }
        return success;
    }

    @Override
    public List<User> getChannelParticipants(UUID channelId) {
        Channel channel = channelRepository.findById(channelId);
        if (channel == null) {
            return null;
        }
        return channel.getParticipants();
    }

    @Override
    public void deleteChannel(UUID id) {
        Channel channel = channelRepository.findById(id);
        if (channel != null) {
            for (User participant : channel.getParticipants()) {
                participant.getJoinedChannels().remove(channel);
            }
            channel.getOwner().getOwnedChannels().remove(channel);
            channelRepository.deleteById(id);
        }
    }
}
