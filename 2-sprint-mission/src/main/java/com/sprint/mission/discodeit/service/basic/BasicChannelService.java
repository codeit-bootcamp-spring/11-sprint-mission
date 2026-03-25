package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    @Override
    public ChannelDto.Response createPublicChannel(ChannelDto.CreatePublicRequest request) {
        Channel channel = request.toEntity();
        channelRepository.save(channel);
        return ChannelDto.Response.of(channel, null, null);
    }

    @Override
    public ChannelDto.Response createPrivateChannel(ChannelDto.CreatePrivateRequest request) {
        Channel channel = request.toEntity();
        channelRepository.save(channel);

        // 채널 참여자 ReadStatus 생성
        if (request.memberIds() != null) {
            request.memberIds().forEach(userId -> {
                ReadStatus readStatus = ReadStatus.builder()
                        .userId(userId)
                        .channelId(channel.getId())
                        .build();
                readStatusRepository.save(readStatus);
            });
        }

        return ChannelDto.Response.of(channel, null, request.memberIds());
    }

    // 공통 로직
    private ChannelDto.Response toResponse(Channel channel) {
        // 가장 최신 메시지
        Instant lastMessageAt = messageRepository.findAll().stream()
                .filter(m -> m.getChannelId().equals(channel.getId()))
                .map(BaseEntity::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);

        // 채널 참여 멤버 목록
        List<UUID> userIds = null;
        if (channel.getType() == ChannelType.PRIVATE) {
            userIds = readStatusRepository.findAll().stream()
                    .filter(rs -> rs.getChannelId().equals(channel.getId()))
                    .map(ReadStatus::getUserId)
                    .toList();
        }

        return ChannelDto.Response.of(channel, lastMessageAt, userIds);
    }

    @Override
    public ChannelDto.Response findById(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Channel not found with id " + id));

        return toResponse(channel);
    }

    @Override
    public List<ChannelDto.Response> findAllByUserId(UUID userId) {
        List<UUID> myChannelIds = readStatusRepository.findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .map(ReadStatus::getChannelId)
                .toList();

        return channelRepository.findAll().stream()
                .filter(channel -> channel.getType() == ChannelType.PUBLIC || myChannelIds.contains(channel.getId()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ChannelDto.Response update(UUID id, ChannelDto.UpdateRequest request) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Channel not found with id " + id));


        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalStateException("PRIVATE channels cannot be updated.");
        }

        channel.update(request.name(), request.description());
        return toResponse(channel);
    }

    @Override
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Channel not found with id " + id));

        // 채널 내 메시지 삭제
        messageRepository.findAll().stream()
                .filter(m -> m.getChannelId().equals(id))
                .toList()
                .forEach(m -> messageRepository.deleteById(m.getId()));

        // 채널 내 ReadStatus 삭제
        readStatusRepository.findAll().stream()
                .filter(rs -> rs.getChannelId().equals(id))
                .toList()
                .forEach(rs -> readStatusRepository.deleteById(rs.getId()));


        channelRepository.deleteById(id);
    }
}