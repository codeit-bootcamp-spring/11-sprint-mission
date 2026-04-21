package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.service.dto.channel.CreatePrivateChannelRequest;
import com.sprint.mission.discodeit.service.dto.channel.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.service.dto.channel.UpdateChannelRequest;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final ChannelMapper channelMapper;

    @Transactional
    public ChannelDto createPublicChannel(CreatePublicChannelRequest request) {
        validatePublicChannelRequest(request);
        Channel savedChannel = channelRepository.save(
                Channel.publicChannel(request.name(), request.description())
        );
        return toDto(savedChannel);
    }

    @Transactional
    public ChannelDto createPrivateChannel(CreatePrivateChannelRequest request) {
        validatePrivateChannelRequest(request);

        Channel savedChannel = channelRepository.save(Channel.privateChannel());

        request.participantIds().stream()
                .distinct()
                .forEach(participantId -> {
                    User user = userRepository.findById(participantId)
                            .orElseThrow(() -> new DiscodeitException(ErrorCode.USER_NOT_FOUND));
                    readStatusRepository.save(
                            new ReadStatus(user, savedChannel, savedChannel.getCreatedAt())
                    );
                });

        return toDto(savedChannel);
    }

    public ChannelDto find(UUID id) {
        return toDto(getChannel(id));
    }

    public List<ChannelDto> findAllByUserId(UUID userId) {
        if (userId == null) {
            throw new DiscodeitException(ErrorCode.USER_ID_REQUIRED);
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new DiscodeitException(ErrorCode.USER_NOT_FOUND));

        List<UUID> visiblePrivateChannelIds = readStatusRepository.findAllByUserId(userId).stream()
                .map(readStatus -> readStatus.getChannel().getId())
                .distinct()
                .toList();

        return channelRepository.findAll().stream()
                .filter(channel -> isVisibleChannel(channel, visiblePrivateChannelIds))
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ChannelDto update(UpdateChannelRequest request) {
        validateUpdateChannelRequest(request);

        Channel channel = getChannel(request.channelId());
        if (channel.getType() == ChannelType.PRIVATE) {
            throw new DiscodeitException(ErrorCode.PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED);
        }

        channel.update(request.name(), request.description());
        return toDto(channel);
    }

    @Transactional
    public void delete(UUID id) {
        Channel channel = getChannel(id);

        // 채널 연관 메시지 삭제: Message 엔티티의 attachments는 CascadeType.ALL + orphanRemoval이므로
        // 엔티티를 로드하여 삭제하면 첨부파일까지 자동 삭제됨
        List<Message> messages = messageRepository.findAllByChannelId(id);
        messageRepository.deleteAll(messages);

        readStatusRepository.deleteAllByChannelId(id);
        channelRepository.delete(channel);
    }

    private Channel getChannel(UUID id) {
        if (id == null) {
            throw new DiscodeitException(ErrorCode.CHANNEL_ID_REQUIRED);
        }
        return channelRepository.findById(id)
                .orElseThrow(() -> new DiscodeitException(ErrorCode.CHANNEL_NOT_FOUND));
    }

    private ChannelDto toDto(Channel channel) {
        return channelMapper.toDto(channel);
    }

    private void validatePublicChannelRequest(CreatePublicChannelRequest request) {
        if (request == null) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "공개 채널 생성 요청값이 비어있어요.");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "공개 채널 이름이 비어있어요.");
        }
    }

    private void validatePrivateChannelRequest(CreatePrivateChannelRequest request) {
        if (request == null) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "비공개 채널 생성 요청값이 비어있어요.");
        }
        if (request.participantIds() == null || request.participantIds().isEmpty()) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "비공개 채널 참여자는 최소 1명 이상 필요해요.");
        }
    }

    private void validateUpdateChannelRequest(UpdateChannelRequest request) {
        if (request == null) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "채널 수정 요청값이 비어있어요.");
        }
        if (request.channelId() == null) {
            throw new DiscodeitException(ErrorCode.CHANNEL_ID_REQUIRED);
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "채널 이름이 비어있어요.");
        }
    }

    private boolean isVisibleChannel(Channel channel, List<UUID> visiblePrivateChannelIds) {
        if (channel.getType() == ChannelType.PUBLIC) {
            return true;
        }
        return visiblePrivateChannelIds.contains(channel.getId());
    }
}
