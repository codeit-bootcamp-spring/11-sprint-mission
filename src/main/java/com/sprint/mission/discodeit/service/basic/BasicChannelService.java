package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        if (this.channelRepository.existByName(publicChannelCreateRequest.name()))
            throw new IllegalArgumentException("name cannot be duplicated. ❌");

        Channel channel = new Channel(publicChannelCreateRequest);
        this.channelRepository.save(channel);

        log.info("{} channel has been created successfully. ✅ [ID: {}]", channel.getName(), channel.getId());
        return channel.toResponse(new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public ChannelResponse createPrivateChannel(PrivateChannelCreateRequest privateChannelCreateRequest) {
        if (privateChannelCreateRequest.participants() == null || privateChannelCreateRequest.participants().isEmpty())
            throw new IllegalArgumentException("participants is required. ❌");

        Channel channel = new Channel();
        this.channelRepository.save(channel);

        List<UUID> participants = privateChannelCreateRequest.participants().stream()
                .distinct()
                .filter(this.userRepository::existById)
                .toList();

        if (participants.isEmpty())
            throw new IllegalArgumentException("no valid participants found. ❌");

        participants.forEach(userId -> {
            ReadStatus status = new ReadStatus(userId, channel.getId());
            this.readStatusRepository.save(status);
        });

        log.info("private channel has been created successfully. ✅ [ID: {}]", channel.getId());
        return channel.toResponse(new ArrayList<>(), participants);
    }

    @Override
    public ChannelResponse findById(UUID id) {
        Channel channel = this.channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested channel not found. ❌"));
        List<Message> messages = this.messageRepository.findAllByChannelId(channel.getId());
        List<UUID> participants = this.readStatusRepository.findAllByChannelId(channel.getId()).stream()
                .map(ReadStatus::getUserId)
                .toList();
        return channel.toResponse(messages, participants);
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        Set<UUID> joinedPrivateChannelIds = this.readStatusRepository.findAllByUserId(userId).stream()
                .map(ReadStatus::getChannelId)
                .collect(Collectors.toSet());

        return this.channelRepository.findAll().stream()
                .filter(channel -> !channel.isPrivate() || joinedPrivateChannelIds.contains(channel.getId()))
                .map(channel -> {
                    List<Message> messages = this.messageRepository.findAllByChannelId(channel.getId());
                    List<UUID> participants = this.readStatusRepository.findAllByChannelId(channel.getId()).stream()
                            .map(ReadStatus::getUserId)
                            .toList();
                    return channel.toResponse(messages, participants);
                })
                .toList();
    }

    @Override
    public ChannelResponse updateChannel(UUID id, ChannelUpdateRequest channelUpdateRequest) {
        Channel channel = this.channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested channel not found. ❌"));

        if (channel.isPrivate()) throw new IllegalArgumentException("private channel cannot be updated. ❌");

        if (channelUpdateRequest.name() != null && !channelUpdateRequest.name().isBlank()) {
            if (!channel.getName().equals(channelUpdateRequest.name()) && this.channelRepository.existByName(channelUpdateRequest.name()))
                throw new IllegalArgumentException("name cannot be duplicated. ❌");
            channel.updateName(channelUpdateRequest.name());
        }

        if (channelUpdateRequest.description() != null) channel.updateDescription(channelUpdateRequest.description());

        this.channelRepository.save(channel);

        List<Message> messages = this.messageRepository.findAllByChannelId(channel.getId());
        List<UUID> participants = this.readStatusRepository.findAllByChannelId(channel.getId()).stream()
                .map(ReadStatus::getUserId)
                .toList();

        log.info("{} channel has been updated successfully. ✅ [ID: {}]", channel.getName(), channel.getId());
        return channel.toResponse(messages, participants);
    }

    @Override
    public void deleteChannel(UUID id) {
        Channel channel = this.channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested channel not found. ❌"));

        this.messageRepository.deleteAllByChannelId(channel.getId());
        this.readStatusRepository.deleteAllByChannelId(channel.getId());
        this.channelRepository.delete(channel);

        log.info("{} channel has been deleted successfully. ✅ [ID: {}]", channel.getName(), id);
    }
}
