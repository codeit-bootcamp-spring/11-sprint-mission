package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    public Channel createPublicChannel(PublicChannelCreateRequest dto) {
        Channel channel = new Channel(dto.name(), dto.description(), ChannelType.PUBLIC);
        return channelRepository.save(channel);
    }

    @Override
    public Channel createPrivateChannel(PrivateChannelCreateRequest dto) {
        if (dto.users() == null || dto.users().size() < 2) {
            throw new IllegalArgumentException("채널에 참여할 유저가 최소 2명 이상이어야 합니다.");
        }
        Channel channel = new Channel(dto.name(), null, ChannelType.PRIVATE);
        dto.users().forEach(i -> {
            User user = userRepository.findById(i)
                    .orElseThrow(() -> new IllegalArgumentException("없는 user 입니다."));
            ReadStatus status = new ReadStatus(user.getId(), channel.getId(), Instant.now());
            readStatusRepository.save(status);
        });
        channelRepository.save(channel);
        return channel;
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        // 한 유저가 속한 PRIVATE 채팅방과, 공개방인 PUBLIC 채팅방 목록을 보여주는 메서드
        // refactor: 비효율 로직 개선 - findAll은 한 번만 쓰고 filter로 골라내기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId 입니다."));

        Set<UUID> participatedChannels = readStatusRepository.findAllByUserId(user.getId()).stream()
                .map(ReadStatus::getChannelId)
                .collect(Collectors.toSet());

        return channelRepository.findAll().values().stream()
                .filter(c -> c.getType().equals(ChannelType.PUBLIC) ||
                participatedChannels.contains(c.getId()))
                .map(this::toDto)
                .toList();
    }

    @Override
    public ChannelResponse findById(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("없는 channel id 입니다."));

            return toDto(channel);
    }

    @Override
    public List<UUID> getUserIds(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 channel id 입니다."));
        List<ReadStatus> readStatusList = readStatusRepository.findAllByChannelId(channel.getId());
        return readStatusList.stream()
                .map(ReadStatus::getUserId)
                .toList();
    }

    @Override
    public Channel update(UUID id, ChannelUpdateRequest dto) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 channel id 입니다."));
        if (channel.getType().equals(ChannelType.PRIVATE)) {
            throw new IllegalArgumentException("채널의 타입이 수정 불가한 PRIVATE 입니다.");
        }
        channel.update(dto.name(), dto.description());
        return channelRepository.save(channel);
    }

    @Override
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 channel id 입니다."));
        readStatusRepository.deleteByChannelId(channel.getId());
        messageRepository.deleteByChannelId(channel.getId());
        channelRepository.delete(channel.getId());
    }

    private ChannelResponse toDto(Channel channel) {
        Instant latestMessageTime = messageRepository.findLatestMessageByChannelId(channel.getId())
                .map(Message::getCreatedAt)
                .orElse(null);

        List<UUID> userIdList = List.of();
        if (channel.getType().equals(ChannelType.PRIVATE)) {
            userIdList = readStatusRepository.findAllByChannelId(channel.getId()).stream()
                    .map(ReadStatus::getUserId)
                    .toList();
        }

        return new ChannelResponse(channel.getId(), channel.getName(), channel.getDescription(), channel.getType(), latestMessageTime, userIdList);
    }
}
