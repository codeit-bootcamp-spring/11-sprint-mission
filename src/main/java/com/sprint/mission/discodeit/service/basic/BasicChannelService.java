package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.InvalidException;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelMapper channelMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('CHANNEL_MANAGER')")
    public ChannelDto createPublic(PublicChannelCreateRequest request) {
        log.info("PUBLIC 채널 생성 시작: name={}", request.name());

        Channel channel = new Channel(request.name(), request.description());
        Channel savedChannel = channelRepository.save(channel);

        log.info("PUBLIC 채널 생성 완료: channelId={}", savedChannel.getId());

        return channelMapper.toDto(savedChannel, List.of(), null);
    }

    @Override
    @Transactional
    public ChannelDto createPrivate(PrivateChannelCreateRequest request) {
        log.debug("PRIVATE 채널 참여자 목록: participantIds={}",
                request.participantIds());

        List<User> participants = request.participantIds().stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> {
                            log.warn("PRIVATE 채널 생성 실패 - 사용자 없음: userId={}", userId);
                            return new UserNotFoundException(userId);
                        }))
                .toList();

        Channel channel = Channel.createPrivateChannel();
        Channel savedChannel = channelRepository.save(channel);

        for (User user : participants) {
            ReadStatus readStatus = new ReadStatus(user, savedChannel, Instant.now());
            readStatusRepository.save(readStatus);
        }

        List<UserDto> participantDtos = participants.stream()
                .map(userMapper::toDto)
                .toList();

        log.info("PRIVATE 채널 생성 완료: channelId={}, participantCount={}",
                savedChannel.getId(),
                participants.size()
        );

        return channelMapper.toDto(savedChannel, participantDtos, null);
    }

    @Override
    public Optional<ChannelDto> find(UUID id) {
        return channelRepository.findById(id)
                .map(channel -> {
                    Map<UUID, Instant> lastMessageAtMap = messageRepository
                            .findLastMessageTimesByChannelIds(List.of(channel.getId()))
                            .stream()
                            .collect(Collectors.toMap(
                                    row -> (UUID) row[0],
                                    row -> (Instant) row[1]
                            ));

                    Map<UUID, List<UserDto>> participantsMap = readStatusRepository
                            .findAllWithUserByChannelIds(List.of(channel.getId()))
                            .stream()
                            .collect(Collectors.groupingBy(
                                    rs -> rs.getChannel().getId(),
                                    Collectors.mapping(rs -> userMapper.toDto(rs.getUser()), Collectors.toList())
                            ));

                    return channelMapper.toDto(
                            channel,
                            participantsMap.getOrDefault(channel.getId(), List.of()),
                            lastMessageAtMap.get(channel.getId())
                    );
                });
    }

    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        log.debug("사용자별 채널 목록 조회 시작: userId={}", userId);

        List<Channel> channels = channelRepository.findVisibleChannelsByUserId(userId);

        if (channels.isEmpty()) {
            log.debug("사용자별 채널 목록 조회 결과 없음: userId={}", userId);
            return List.of();
        }

        List<UUID> channelIds = channels.stream()
                .map(Channel::getId)
                .toList();

        Map<UUID, Instant> lastMessageAtMap = messageRepository.findLastMessageTimesByChannelIds(channelIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Instant) row[1]
                ));

        Map<UUID, List<UserDto>> participantsMap = readStatusRepository.findAllWithUserByChannelIds(channelIds).stream()
                .collect(Collectors.groupingBy(
                        rs -> rs.getChannel().getId(),
                        Collectors.mapping(rs -> userMapper.toDto(rs.getUser()), Collectors.toList())
                ));

      List<ChannelDto> result = channels.stream()
                .map(channel -> channelMapper.toDto(
                        channel,
                        participantsMap.getOrDefault(channel.getId(), List.of()),
                        lastMessageAtMap.get(channel.getId())
                ))
                .toList();

        log.debug("사용자별 채널 목록 조회 완료: userId={}, count={}", userId, result.size());

        return result;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('CHANNEL_MANAGER')")
    public ChannelDto update(ChannelUpdateParam param) {
        log.info("채널 수정 시작: channelId={}", param.id());

        Channel channel = channelRepository.findById(param.id())
                .orElseThrow(() -> {
                    log.warn("채널 수정 실패 - 채널 없음: channelId={}", param.id());
                    return new ChannelNotFoundException(param.id());
                });

        if (channel.getType() == ChannelType.PRIVATE) {
            log.warn("채널 수정 실패 - PRIVATE 채널 수정 시도: channelId={}", channel.getId());
            throw new PrivateChannelUpdateException(channel.getId());
        }

        channel.update(
                param.request().newName(),
                param.request().newDescription()
        );

        log.info("채널 수정 완료: channelId={}", channel.getId());

        return channelMapper.toDto(channel, List.of(), null);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('CHANNEL_MANAGER')")
    public void delete(UUID id) {
        log.info("채널 삭제 시작: channelId={}", id);

        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("채널 삭제 실패 - 채널 없음: channelId={}", id);
                    return new ChannelNotFoundException(id);
                });

        channelRepository.delete(channel);

        log.info("채널 삭제 완료: channelId={}", id);
    }
}
