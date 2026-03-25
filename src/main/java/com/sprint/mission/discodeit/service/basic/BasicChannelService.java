package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelInfoDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateDto;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateDto;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateDto;
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
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    public Channel createPublicChannel(PublicChannelCreateDto dto) {
        Channel channel = new Channel(dto.name(), dto.description(), ChannelType.PUBLIC);
        return channelRepository.save(channel);
    }

    @Override
    public Channel createPrivateChannel(PrivateChannelCreateDto dto) {
        if (dto.users() == null || dto.users().size() < 2) {
            throw new IllegalArgumentException("채널에 참여할 유저가 최소 2명 이상이어야 합니다.");
        }
        Channel channel = new Channel(null, null, ChannelType.PRIVATE);
        dto.users().forEach(i -> {
            User user = userRepository.findById(i)
                    .orElseThrow(() -> new IllegalArgumentException("없는 user 입니다."));
            ReadStatus status = new ReadStatus(user.getId(), channel.getId());
            readStatusRepository.save(status);
        });
        channelRepository.save(channel);
        return channel;
    }

    @Override
    public List<ChannelInfoDto> findAllByUserId(UUID userId) {
        // 한 유저가 속한 PRIVATE 채팅방과, 공개방인 PUBLIC 채팅방 목록을 보여주는 메서드
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId 입니다."));

        List<ChannelInfoDto> privateChannels = readStatusRepository.findAllByUserId(user.getId()).stream()
                .map(ReadStatus::getChannelId)
                .map(this::findById)
                .toList();

        List<ChannelInfoDto> publicChannels = channelRepository.findAll().values().stream()
                .filter(c -> c.getType().equals(ChannelType.PUBLIC))
                .map(c -> this.findById(c.getId()))
                .toList();

        return Stream.concat(privateChannels.stream(), publicChannels.stream()).toList();
    }

    @Override
    public ChannelInfoDto findById(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("없는 channel id 입니다."));

        Optional<Message> message = messageRepository.findLatestMessageByChannelId(channel.getId());

        Instant latestMessageTime = message.map(Message::getCreatedAt)
                .orElse(null);

        if (channel.getType().equals(ChannelType.PRIVATE)) {
            List<ReadStatus> status = readStatusRepository.findAllByChannelId(channel.getId());

            List<UUID> userIdList = status.stream()
                    .map(ReadStatus::getUserId)
                    .toList();

            return new ChannelInfoDto(channel.getId(), channel.getName(), channel.getDescription(),
                    channel.getType(), latestMessageTime, userIdList);
        }

        return new ChannelInfoDto(channel.getId(), channel.getName(), channel.getDescription(),
                channel.getType(), latestMessageTime, List.of());
    }

    @Override
    public ChannelInfoDto update(ChannelUpdateDto dto) {
        Channel channel = channelRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("없는 channel id 입니다."));
        if (channel.getType().equals(ChannelType.PRIVATE)) {
            throw new IllegalArgumentException("채널의 타입이 수정 불가한 PRIVATE 입니다.");
        }
        channel.update(dto.name(), dto.description());
        channelRepository.save(channel);

        return findById(channel.getId());
    }

    @Override
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 channel id 입니다."));
        readStatusRepository.deleteByChannelId(channel.getId());
        messageRepository.deleteByChannelId(channel.getId());
        channelRepository.delete(channel.getId());
    }
}
