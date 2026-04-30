package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.projection.ChannelLastMessageAtProjection;
import com.sprint.mission.discodeit.dto.projection.ChannelParticipantProjection;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelParticipantDuplicatedException;
import com.sprint.mission.discodeit.exception.channel.ChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.exception.channel.InvalidParticipantIdException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepo;
    private final MessageRepository messageRepo;
    private final ReadStatusRepository readStatusRepo;
    private final UserRepository userRepo;
    private final ChannelMapper channelMapper;
    private final BinaryContentService binaryContentService;

    @Override
    @Transactional
    public ChannelDto createPublicChannel(PublicChannelCreateRequest dto) {
        Channel channel = new Channel(ChannelType.PUBLIC, dto.name(), dto.description());
        channelRepo.save(channel);

        log.info("Public channel created. channelId={}, name={}", channel.getId(), channel.getName());

        return channelMapper.toDto(
                channel,
                List.of(),
                null
        );
    }

    @Override
    @Transactional
    public ChannelDto createPrivateChannel(PrivateChannelCreateRequest dto) {
        List<UUID> participantIds = dto.participantIds();
        Set<UUID> uniqueParticipantIds = new HashSet<>(participantIds);
        if(uniqueParticipantIds.size() != participantIds.size()) {
            throw new ChannelParticipantDuplicatedException(participantIds);
        }

        List<User> participants = userRepo.findAllById(uniqueParticipantIds);
        if(participants.size() != uniqueParticipantIds.size()) {
            Set<UUID> foundParticipantIds = participants.stream()
                    .map(User::getId)
                    .collect(Collectors.toSet());
            List<UUID> invalidParticipantIds = uniqueParticipantIds.stream()
                    .filter(participantId -> !foundParticipantIds.contains(participantId))
                    .toList();
            throw new InvalidParticipantIdException(invalidParticipantIds);
        }

        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        channelRepo.save(channel);

        List<ReadStatus> readStatuses = participants.stream()
                .map(user -> new ReadStatus(user, channel, Instant.now()))
                .toList();

        readStatusRepo.saveAll(readStatuses);

        log.info("Private channel created. channelId={}, participantCount={}", channel.getId(), participantIds.size());

        return channelMapper.toDto(
                channel,
                participants,
                null
        );
    }

    @Override
    public ChannelDto findById(UUID id) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));

        Instant lastMessageAt = messageRepo.findLastMessageAtByChannel(channel)
                .orElse(null);

        List<User> participants = channel.getChannelType() == ChannelType.PRIVATE
                ? readStatusRepo.findUsersByChannel(channel)
                : List.of();

        return channelMapper.toDto(channel, participants, lastMessageAt);
    }

    @Override
    public List<ChannelDto> findAllByUserId(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        List<Channel> publicChannels = channelRepo.findAllByChannelType(ChannelType.PUBLIC);
        List<Channel> privateChannels = channelRepo.findChannelsByUser(user);

        List<Channel> allChannels = new ArrayList<>();
        allChannels.addAll(publicChannels);
        allChannels.addAll(privateChannels);

        if(allChannels.isEmpty()) {
            return List.of();
        }

        Map<UUID, Instant> lastMessageAtMap = messageRepo.findLastMessageAtByChannels(allChannels).stream()
                .collect(Collectors.toMap(
                        ChannelLastMessageAtProjection::channelId,
                        ChannelLastMessageAtProjection::lastMessageAt
                ));
        Map<UUID, List<User>> participantsMap = readStatusRepo.findUsersByChannels(privateChannels).stream()
                .collect(Collectors.groupingBy(
                        ChannelParticipantProjection::channelId,
                        Collectors.mapping(ChannelParticipantProjection::participant, Collectors.toList())
                ));

        return allChannels.stream()
                .map(channel -> channelMapper.toDto(
                        channel,
                        participantsMap.getOrDefault(channel.getId(), List.of()),
                        lastMessageAtMap.get(channel.getId())
                ))
                .toList();
    }

    @Override
    @Transactional
    public void update(UUID id, ChannelUpdateRequest dto) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));

        if(channel.getChannelType() == ChannelType.PRIVATE)
            throw new ChannelUpdateNotAllowedException(id);

        channel.update(dto.newName(), dto.newDescription());

        log.info("Channel updated. channelId={}", channel.getId());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));

        List<Message> messages = messageRepo.findAllWithAttachmentsByChannel(channel);

        List<BinaryContent> attachments = messages.stream()
                .flatMap(message -> message.getAttachments().stream())
                .toList();

        binaryContentService.deleteAll(attachments);

        channelRepo.delete(channel);
        log.info("Channel deleted. channelId={}, attachmentCount = {}", channel.getId(), attachments.size());
    }
}
