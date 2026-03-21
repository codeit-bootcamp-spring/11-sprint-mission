package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public BasicChannelService(ChannelRepository channelRepository, UserRepository userRepository, MessageRepository messageRepository) {
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public Channel createChannel(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required. ❌");
        if (existChannelByName(name)) throw new IllegalArgumentException("name cannot be duplicated. ❌");

        Channel channel = new Channel(name);
        this.channelRepository.save(channel);

        log.info("{} channel has been created successfully. ✅ [ID: {}]", name, channel.getId());
        return channel;
    }

    @Override
    public Channel getChannelById(UUID id) {
        return this.channelRepository.findById(id);
    }

    @Override
    public boolean existChannelByName(String name) {
        return this.channelRepository.existByName(name);
    }

    @Override
    public List<Channel> getAllChannels() {
        return this.channelRepository.findAll();
    }

    @Override
    public Channel updateChannel(UUID id, String name) {
        Channel channel = this.getChannelById(id);

        if (name != null && !name.isBlank()) {
            if (existChannelByName(name)) throw new IllegalArgumentException("name cannot be duplicated. ❌");
            channel.updateName(name);
        }

        this.channelRepository.save(channel);

        log.info("{} channel has been updated successfully. ✅ [ID: {}]", name, id);
        return channel;
    }

    @Override
    public void deleteChannel(UUID id) {
        Channel channel = this.getChannelById(id);

        channel.getParticipants()
                .forEach(user -> {
                    user.getChannels().removeIf(ch -> ch.getId().equals(channel.getId()));
                    this.userRepository.save(user);
                });

        channel.getMessages()
                .forEach(message -> {
                    User sender = message.getSender();
                    sender.getMessages().remove(message);
                    this.userRepository.save(sender);
                    this.messageRepository.delete(message);
                });

        this.channelRepository.delete(channel);

        log.info("{} channel has been deleted successfully. ✅ [ID: {}]", channel.getName(), id);
    }

    @Override
    public void joinChannel(UUID id, UUID participantId) {
        Channel channel = this.getChannelById(id);
        User participant = this.userRepository.findById(participantId);

        if (channel.getParticipants().stream().anyMatch(p -> p.getId().equals(participant.getId()))) throw new IllegalArgumentException("duplicated participation is not allowed. ❌");

        channel.getParticipants().add(participant);
        this.channelRepository.save(channel);

        participant.getChannels().add(channel);
        this.userRepository.save(participant);

        log.info("{} has joined {} channel successfully. ✅", participant.getNickname(), channel.getName());
    }

    @Override
    public void leaveChannel(UUID id, UUID participantId) {
        Channel channel = this.getChannelById(id);
        User participant = this.userRepository.findById(participantId);

        channel.getParticipants().removeIf(p -> p.getId().equals(participant.getId()));
        this.channelRepository.save(channel);

        participant.getChannels().removeIf(ch -> ch.getId().equals(channel.getId()));
        this.userRepository.save(participant);

        log.info("{} has left {} channel successfully. ✅", participant.getNickname(), channel.getName());
    }
}
