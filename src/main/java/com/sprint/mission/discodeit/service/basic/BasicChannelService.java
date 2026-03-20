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
        Channel channel = new Channel();
        channelRepository.save(channel);

        for (UUID userId : request.getUserIds()) {
            ReadStatus readStatus = new ReadStatus(userId, channel.getId());
            readStatusRepository.save(readStatus);
        }

        return toResponse(channel);
    }

    @Override
    public ChannelResponse getChannelById(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Channel입니다."));
        return toResponse(channel);
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        List<UUID> joinedChannelIds = readStatusRepository.findByUserId(userId).stream()
                .map(ReadStatus::getChannelId)
                .toList();

        return channelRepository.findAll().stream()
                .filter(channel ->
                        // PUBLIC 채널은 전체 조회
                        channel.getType() == ChannelType.PUBLIC ||
                                // PRIVATE 채널은 참여한 채널만
                                joinedChannelIds.contains(channel.getId())
                )
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ChannelResponse updateChannel(UUID id, UpdateChannelRequest request) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Channel입니다."));
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
        if (!channelRepository.findById(channelId).isPresent()) {
            throw new IllegalArgumentException("존재하지 않는 Channel입니다.");
        }

        // 이미 참여 중인지 확인
        boolean alreadyJoined = readStatusRepository.findByUserId(userId).stream()
                .anyMatch(rs -> rs.getChannelId().equals(channelId));

        if (alreadyJoined) return false;

        ReadStatus readStatus = new ReadStatus(userId, channelId);
        readStatusRepository.save(readStatus);
        return true;
    }

    @Override
    public boolean leaveChannel(UUID channelId, UUID userId) {
        if (!channelRepository.findById(channelId).isPresent()) {
            throw new IllegalArgumentException("존재하지 않는 Channel입니다.");
        }

        ReadStatus readStatus = readStatusRepository.findByUserIdAndChannelId(userId,channelId)
                .orElse(null);

        if (readStatus == null) return false;

        readStatusRepository.deleteById(readStatus.getId());
        return true;
    }

    @Override
    public boolean kickUser(UUID channelId, UUID ownerId, UUID targetUserId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Channel입니다."));

        if (!channel.getOwnerId().equals(ownerId)) return false;

        ReadStatus readStatus = readStatusRepository.findByUserIdAndChannelId(targetUserId, channelId)
                .orElse(null);

        if (readStatus == null) return false;

        readStatusRepository.deleteById(readStatus.getId());
        return true;
    }

    @Override
    public List<UUID> getChannelParticipants(UUID channelId) {
        if (!channelRepository.findById(channelId).isPresent()) {
            throw new IllegalArgumentException("존재하지 않는 Channel입니다.");
        }

        return readStatusRepository.findByChannelId(channelId).stream()
                .map(ReadStatus::getUserId)
                .toList();
    }

    // Channel → ChannelResponse 변환
    private ChannelResponse toResponse(Channel channel) {
        Instant lastMessageAt = messageRepository.findByChannelId(channel.getId()).stream()
                .map(Message::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);

        List<UUID> participantIds = channel.getType() == ChannelType.PRIVATE
                ? readStatusRepository.findByChannelId(channel.getId()).stream()
                        .map(ReadStatus::getUserId)
                        .toList()
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