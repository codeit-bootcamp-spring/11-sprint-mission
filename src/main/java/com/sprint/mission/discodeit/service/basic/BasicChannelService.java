package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    @Override
    public ChannelResponse createPublicChannel(PublicChannelCreateRequest publicChannelCreateRequest) {
        if (publicChannelCreateRequest.name() == null || publicChannelCreateRequest.name().isBlank())
            throw new IllegalArgumentException("name is required. ❌");
        if (this.existChannelByName(publicChannelCreateRequest.name()))
            throw new IllegalArgumentException("name cannot be duplicated. ❌");

        Channel channel = new Channel(publicChannelCreateRequest);
        this.channelRepository.save(channel);

        log.info("{} channel has been created successfully. ✅ [ID: {}]", channel.getName(), channel.getId());
        return channel.toResponse();
    }

    @Override
    public ChannelResponse createPrivateChannel(PrivateChannelCreateRequest privateChannelCreateRequest) {
        if (privateChannelCreateRequest.participants() == null || privateChannelCreateRequest.participants().isEmpty())
            throw new IllegalArgumentException("participants is required. ❌");

        List<User> participants = privateChannelCreateRequest.participants().stream()
                .distinct()
                .map(userId -> this.userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("requested user not found. ❌")))
                .toList();

        Channel channel = new Channel(participants);
        this.channelRepository.save(channel);

        channel.getParticipants().forEach(user -> {
            user.getChannels().add(channel);
            this.userRepository.save(user);
            ReadStatus status = new ReadStatus(user, channel);
            this.readStatusRepository.save(status);
        });

        log.info("private channel has been created successfully. ✅ [ID: {}]", channel.getId());
        return channel.toResponse();
    }

    @Override
    public ChannelResponse findById(UUID id) {
        return this.channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested channel not found. ❌"))
                .toResponse();
    }

    @Override
    public boolean existChannelByName(String name) {
        return this.channelRepository.existByName(name);
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        return this.channelRepository.findAll().stream()
                .filter(channel -> !channel.isPrivate()
                        || channel.getParticipants().stream().anyMatch(p -> p.getId().equals(userId)))
                .map(Channel::toResponse)
                .toList();
    }

    @Override
    public ChannelResponse updateChannel(UUID id, ChannelUpdateRequest channelUpdateRequest) {
        Channel channel = this.channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested channel not found. ❌"));

        if (channel.isPrivate()) throw new IllegalArgumentException("private channel cannot be updated. ❌");

        if (channelUpdateRequest.name() != null && !channelUpdateRequest.name().isBlank()) {
            if (!channel.getName().equals(channelUpdateRequest.name()) && this.existChannelByName(channelUpdateRequest.name()))
                throw new IllegalArgumentException("name cannot be duplicated. ❌");
            channel.updateName(channelUpdateRequest.name());
        }

        if (channelUpdateRequest.description() != null) channel.updateDescription(channelUpdateRequest.description());

        this.channelRepository.save(channel);

        log.info("{} channel has been updated successfully. ✅ [ID: {}]", channel.getName(), id);
        return channel.toResponse();
    }

    @Override
    public void deleteChannel(UUID id) {
        Channel channel = this.channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested channel not found. ❌"));

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

        this.readStatusRepository.deleteByChannelId(channel.getId());

        this.channelRepository.delete(channel);

        log.info("{} channel has been deleted successfully. ✅ [ID: {}]", channel.getName(), id);
    }

    @Override
    public void joinChannel(UUID id, UUID participantId) {
        Channel channel = this.channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested channel not found. ❌"));
        User participant = this.userRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("requested user not found. ❌"));

        if (channel.getParticipants().stream().anyMatch(p -> p.getId().equals(participant.getId())))
            throw new IllegalArgumentException("duplicated participation is not allowed. ❌");

        channel.getParticipants().add(participant);
        this.channelRepository.save(channel);

        participant.getChannels().add(channel);
        this.userRepository.save(participant);

        log.info("{} has joined {} channel successfully. ✅", participant.getNickname(), channel.getName());
    }

    @Override
    public void leaveChannel(UUID id, UUID participantId) {
        Channel channel = this.channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested channel not found. ❌"));
        User participant = this.userRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("requested user not found. ❌"));

        channel.getParticipants().removeIf(p -> p.getId().equals(participant.getId()));
        this.channelRepository.save(channel);

        participant.getChannels().removeIf(ch -> ch.getId().equals(channel.getId()));
        this.userRepository.save(participant);

        log.info("{} has left {} channel successfully. ✅", participant.getNickname(), channel.getName());
    }
}
