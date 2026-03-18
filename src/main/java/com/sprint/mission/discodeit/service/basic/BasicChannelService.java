package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.channel.CreatePrivateChannelRequest;
import com.sprint.mission.discodeit.dto.request.channel.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.dto.request.channel.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    @Override
    public ChannelResponse createPublicChannel(CreatePublicChannelRequest request) {
        Channel channel = new Channel(request.getName(), request.getDescription(), null);
        channelRepository.save(channel);
        return toResponse(channel);
    }

    @Override
    public ChannelResponse createPrivateChannel(CreatePrivateChannelRequest request) {
        Channel channel = new Channel(request.getUserIds());
        channelRepository.save(channel);

        for (UUID userId : request.getUserIds()) {
            ReadStatus readStatus = new ReadStatus(userId, channel.getId());
            readStatusRepository.save(readStatus);
        }

        return toResponse(channel);
    }

    @Override
    public ChannelResponse getChannelById(UUID id) {
        Channel channel = channelRepository.findById(id);
        return toResponse(channel);
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        return channelRepository.findAll().stream()
                .filter(channel ->
                        // PUBLIC 채널은 전체 조회
                        channel.getType() == ChannelType.PUBLIC ||
                                // PRIVATE 채널은 참여한 채널만
                                channel.getParticipantIds().contains(userId)
                )
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ChannelResponse updateChannel(UUID id, UpdateChannelRequest request) {
        Channel channel = channelRepository.findById(id);
        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }
        channel.update(request.getName(), request.getDescription());
        channelRepository.save(channel);
        return toResponse(channel);
    }

    @Override
    public void deleteChannel(UUID id) {
        messageRepository.findByChannelId(id)
                .forEach(message -> messageRepository.deleteById(message.getId()));
        readStatusRepository.findByChannelId(id)
                .forEach(readStatus -> readStatusRepository.deleteById(readStatus.getId()));

        channelRepository.deleteById(id);
    }

    @Override
    public boolean joinChannel(UUID channelId, UUID userId) {
        Channel channel = channelRepository.findById(channelId);
        if (channel == null) return false;
        boolean success = channel.addParticipant(userId);
        if (success) channelRepository.save(channel);
        return success;
    }

    @Override
    public boolean leaveChannel(UUID channelId, UUID userId) {
        Channel channel = channelRepository.findById(channelId);
        if (channel == null) return false;
        boolean success = channel.removeParticipant(userId);
        if (success) channelRepository.save(channel);
        return success;
    }

    @Override
    public boolean kickUser(UUID channelId, UUID ownerId, UUID targetUserId) {
        Channel channel = channelRepository.findById(channelId);
        if (channel == null || !channel.getOwnerId().equals(ownerId)) return false;
        boolean success = channel.removeParticipant(targetUserId);
        if (success) channelRepository.save(channel);
        return success;
    }

    @Override
    public List<UUID> getChannelParticipants(UUID channelId) {
        Channel channel = channelRepository.findById(channelId);
        if (channel == null) return null;
        return channel.getParticipantIds();
    }

    // Channel → ChannelResponse 변환
    private ChannelResponse toResponse(Channel channel) {
        Instant lastMessageAt = messageRepository.findByChannelId(channel.getId()).stream()
                .map(Message::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);

        List<UUID> participantIds = channel.getType() == ChannelType.PRIVATE
                ? channel.getParticipantIds()
                : null;

        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                lastMessageAt,
                participantIds
        );
    }
}