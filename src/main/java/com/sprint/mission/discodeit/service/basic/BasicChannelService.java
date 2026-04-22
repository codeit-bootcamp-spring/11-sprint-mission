package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sprint.mission.discodeit.mapper.ChannelMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final ChannelMapper channelMapper;

    @Override
    @Transactional
    public ChannelDto createPublic(PublicChannelCreateRequest request) {
        Channel channel = new Channel(request.name(), request.description());
        Channel savedChannel = channelRepository.save(channel);
        return channelMapper.toDto(savedChannel);
    }

    @Override
    @Transactional
    public ChannelDto createPrivate(PrivateChannelCreateRequest request) {
        List<User> participants = request.participantIds().stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다.")))
                .toList();

        Channel channel = Channel.createPrivateChannel();
        Channel savedChannel = channelRepository.save(channel);

        for (User user : participants) {
            ReadStatus readStatus = new ReadStatus(user, savedChannel, Instant.now());
            readStatusRepository.save(readStatus);
        }

        return channelMapper.toDto(savedChannel);
    }

    @Override
    public Optional<ChannelDto> find(UUID id) {
        Optional<Channel> optionalChannel = channelRepository.findById(id);

        if (optionalChannel.isEmpty()) {
            return Optional.empty();
        }

        Channel channel = optionalChannel.get();
        return Optional.of(channelMapper.toDto(channel));
    }

    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        return channelRepository.findAll().stream()
                .filter(channel ->
                        channel.getType() == ChannelType.PUBLIC ||
                                readStatusRepository.findAll().stream()
                                        .anyMatch(readStatus ->
                                                readStatus.getChannel().getId().equals(channel.getId()) &&
                                                readStatus.getUser().getId().equals(userId))
                )
                .map(channelMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
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
        return channelMapper.toDto(channel);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 채널이 존재하지 않습니다."));

        messageRepository.findAll().stream()
                .filter(message -> message.getChannel().getId().equals(channel.getId()))
                .forEach(message -> messageRepository.deleteById(message.getId()));

        readStatusRepository.findAll().stream()
                .filter(readStatus -> readStatus.getChannel().getId().equals(channel.getId()))
                .forEach(readStatus -> readStatusRepository.deleteById(readStatus.getId()));

        channelRepository.deleteById(id);
    }
}