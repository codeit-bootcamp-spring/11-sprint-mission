package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateParam;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;

    @Override
    public ChannelDto createPublic(PublicChannelCreateRequest request) {
        Channel channel = new Channel(request.name(), request.description());
        Channel savedChannel = channelRepository.save(channel);

        return toDto(savedChannel);
    }

    @Override
    public ChannelDto createPrivate(PrivateChannelCreateRequest request) {
        for (UUID userId : request.participantIds()) {
            if (userRepository.findById(userId).isEmpty()) {
                throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
            }
        }

        Channel channel = new Channel(request.participantIds());
        Channel savedChannel = channelRepository.save(channel);

        for (UUID userId : request.participantIds()) {
            ReadStatus readStatus = new ReadStatus(userId, savedChannel.getId(), Instant.now());
            readStatusRepository.save(readStatus);
        }

        return toDto(savedChannel);
    }

    @Override
    public Optional<ChannelDto> find(UUID id) {
        Optional<Channel> optionalChannel = channelRepository.findById(id);

        if (optionalChannel.isEmpty()) {
            return Optional.empty();
        }

        Channel channel = optionalChannel.get();
        return Optional.of(toDto(channel));
    }

    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        return channelRepository.findAll().stream()
                .filter(channel ->
                        channel.getType() == ChannelType.PUBLIC ||
                                channel.getParticipantIds().contains(userId)
                )
                .map(this::toDto)
                .toList();
    }

    @Override
    public ChannelDto update(ChannelUpdateParam param) {
        Channel channel = channelRepository.findById(param.id())
                .orElseThrow(() -> new IllegalArgumentException("해당 채널이 존재하지 않습니다."));

        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        channel.update(
                param.request().newName(),
                param.request().newDescription()
        );

        Channel savedChannel = channelRepository.save(channel);
        return toDto(savedChannel);
    }

    @Override
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 채널이 존재하지 않습니다."));

        messageRepository.findAll().stream()
                .filter(message -> message.getChannelId().equals(channel.getId()))
                .forEach(message -> messageRepository.delete(message.getId()));

        readStatusRepository.findAll().stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channel.getId()))
                .forEach(readStatus -> readStatusRepository.delete(readStatus.getId()));

        channelRepository.delete(id);
    }

    private ChannelDto toDto(Channel channel) {
        Instant lastMessageAt = messageRepository.findAllByChannelId(channel.getId()).stream()
                .map(Message::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);

        return new ChannelDto(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getType().name(),
                channel.getParticipantIds(),
                lastMessageAt,
                channel.getCreatedAt(),
                channel.getUpdatedAt()
        );
    }
}