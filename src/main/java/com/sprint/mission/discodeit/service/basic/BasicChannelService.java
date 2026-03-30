package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.ApiException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.*;

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
            throw new ApiException(CHANNEL_NAME_REQUIRED);
        if (this.channelRepository.existByName(publicChannelCreateRequest.name()))
            throw new ApiException(CHANNEL_NAME_DUPLICATED);

        Channel channel = new Channel(publicChannelCreateRequest.name(), publicChannelCreateRequest.description());
        this.channelRepository.save(channel);

        log.info("{} channel has been created successfully. ✅ [ID: {}]", channel.getName(), channel.getId());
        return this.toResponse(channel, new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public ChannelResponse createPrivateChannel(PrivateChannelCreateRequest privateChannelCreateRequest) {
        if (privateChannelCreateRequest.participants() == null || privateChannelCreateRequest.participants().isEmpty())
            throw new ApiException(CHANNEL_PARTICIPANTS_REQUIRED);

        Channel channel = new Channel();
        this.channelRepository.save(channel);

        List<UUID> participants = privateChannelCreateRequest.participants().stream()
                .distinct()
                .filter(this.userRepository::existById)
                .toList();

        if (participants.isEmpty())
            throw new ApiException(CHANNEL_NO_VALID_PARTICIPANTS);

        participants.forEach(userId -> {
            ReadStatus status = new ReadStatus(userId, channel.getId());
            this.readStatusRepository.save(status);
        });

        log.info("private channel has been created successfully. ✅ [ID: {}]", channel.getId());
        return this.toResponse(channel, new ArrayList<>(), participants);
    }

    @Override
    public ChannelResponse findById(UUID id) {
        Channel channel = this.channelRepository.findById(id)
                .orElseThrow(() -> new ApiException(CHANNEL_NOT_FOUND));

        List<Message> messages = this.messageRepository.findAllByChannelId(channel.getId());
        List<UUID> participants = this.readStatusRepository.findAllByChannelId(channel.getId()).stream()
                .map(ReadStatus::getUserId)
                .toList();

        return this.toResponse(channel, messages, participants);
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        Set<UUID> joinedPrivateChannelIds = this.readStatusRepository.findAllByUserId(userId).stream()
                .map(ReadStatus::getChannelId)
                .collect(Collectors.toSet());

        List<Channel> channels = this.channelRepository.findAll().stream()
                .filter(channel -> !channel.isPrivate() || joinedPrivateChannelIds.contains(channel.getId()))
                .toList();

        List<UUID> channelIds = channels.stream().map(Channel::getId).toList();

        Map<UUID, List<Message>> messagesByChannel = this.messageRepository.findAllByChannelIdIn(channelIds).stream()
                .collect(Collectors.groupingBy(Message::getChannelId));

        Map<UUID, List<UUID>> participantsByChannel = this.readStatusRepository.findAllByChannelIdIn(channelIds).stream()
                .collect(Collectors.groupingBy(
                        ReadStatus::getChannelId,
                        Collectors.mapping(ReadStatus::getUserId, Collectors.toList())
                ));

        return channels.stream()
                .map(channel -> this.toResponse(
                        channel,
                        messagesByChannel.getOrDefault(channel.getId(), new ArrayList<>()),
                        participantsByChannel.getOrDefault(channel.getId(), new ArrayList<>())
                ))
                .toList();
    }

    @Override
    public ChannelResponse updateChannel(UUID id, ChannelUpdateRequest channelUpdateRequest) {
        Channel channel = this.channelRepository.findById(id)
                .orElseThrow(() -> new ApiException(CHANNEL_NOT_FOUND));
        if (channel.isPrivate()) throw new ApiException(CHANNEL_PRIVATE_UPDATE_FORBIDDEN);
        if (channelUpdateRequest.name() != null && !channelUpdateRequest.name().isBlank()) {
            if (!channel.getName().equals(channelUpdateRequest.name()) && this.channelRepository.existByName(channelUpdateRequest.name()))
                throw new ApiException(CHANNEL_NAME_DUPLICATED);
            channel.updateName(channelUpdateRequest.name());
        }

        if (channelUpdateRequest.description() != null) channel.updateDescription(channelUpdateRequest.description());

        this.channelRepository.save(channel);

        List<Message> messages = this.messageRepository.findAllByChannelId(channel.getId());
        List<UUID> participants = this.readStatusRepository.findAllByChannelId(channel.getId()).stream()
                .map(ReadStatus::getUserId)
                .toList();

        log.info("{} channel has been updated successfully. ✅ [ID: {}]", channel.getName(), channel.getId());
        return this.toResponse(channel, messages, participants);
    }

    @Override
    public void deleteChannel(UUID id) {
        Channel channel = this.channelRepository.findById(id)
                .orElseThrow(() -> new ApiException(CHANNEL_NOT_FOUND));

        this.messageRepository.deleteAllByChannelId(channel.getId());

        this.readStatusRepository.deleteAllByChannelId(channel.getId());

        this.channelRepository.delete(channel);

        log.info("{} channel has been deleted successfully. ✅ [ID: {}]", channel.getName(), id);
    }

    private ChannelResponse toResponse(Channel channel, List<Message> messages, List<UUID> participants) {
        Instant lastMessageAt = messages.stream()
                .map(Message::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);
        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.isPrivate(),
                lastMessageAt,
                channel.isPrivate() ? participants : null
        );
    }
}
