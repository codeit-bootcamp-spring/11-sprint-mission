package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.dto.readstatus.CreateReadStatusRequest;
import com.sprint.mission.discodeit.service.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.service.dto.readstatus.UpdateReadStatusRequest;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReadStatusMapper readStatusMapper;

    @Transactional
    public ReadStatusDto create(CreateReadStatusRequest request) {
        validateCreateRequest(request);
        User user = getUser(request.userId());
        Channel channel = getChannel(request.channelId());

        if (readStatusRepository.existsByUserIdAndChannelId(request.userId(), request.channelId())) {
            throw new DiscodeitException(ErrorCode.DUPLICATE_READ_STATUS);
        }

        ReadStatus readStatus = new ReadStatus(user, channel, request.lastReadAt());
        return toDto(readStatusRepository.save(readStatus));
    }

    public ReadStatusDto find(UUID id) {
        return toDto(getReadStatus(id));
    }

    public List<ReadStatusDto> findAllByUserId(UUID userId) {
        if (userId == null) {
            throw new DiscodeitException(ErrorCode.USER_ID_REQUIRED);
        }
        getUser(userId);

        return readStatusRepository.findAllByUserId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ReadStatusDto update(UpdateReadStatusRequest request) {
        validateUpdateRequest(request);
        ReadStatus readStatus = getReadStatus(request.readStatusId());
        readStatus.updateLastReadAt(request.lastReadAt());
        return toDto(readStatus);
    }

    @Transactional
    public void delete(UUID id) {
        ReadStatus readStatus = getReadStatus(id);
        readStatusRepository.delete(readStatus);
    }

    private ReadStatus getReadStatus(UUID id) {
        if (id == null) {
            throw new DiscodeitException(ErrorCode.READ_STATUS_ID_REQUIRED);
        }
        return readStatusRepository.findById(id)
                .orElseThrow(() -> new DiscodeitException(ErrorCode.READ_STATUS_NOT_FOUND));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Channel getChannel(UUID channelId) {
        if (channelId == null) {
            throw new DiscodeitException(ErrorCode.CHANNEL_ID_REQUIRED);
        }
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelNotFoundException(channelId));
    }

    private ReadStatusDto toDto(ReadStatus readStatus) {
        return readStatusMapper.toDto(readStatus);
    }

    private void validateCreateRequest(CreateReadStatusRequest request) {
        if (request == null) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "읽음상태 생성 요청값이 비어있어요.");
        }
        if (request.userId() == null) {
            throw new DiscodeitException(ErrorCode.USER_ID_REQUIRED);
        }
        if (request.channelId() == null) {
            throw new DiscodeitException(ErrorCode.CHANNEL_ID_REQUIRED);
        }
    }

    private void validateUpdateRequest(UpdateReadStatusRequest request) {
        if (request == null) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "읽음상태 수정 요청값이 비어있어요.");
        }
        if (request.readStatusId() == null) {
            throw new DiscodeitException(ErrorCode.READ_STATUS_ID_REQUIRED);
        }
        if (request.lastReadAt() == null) {
            throw new DiscodeitException(ErrorCode.LAST_READ_AT_REQUIRED);
        }
    }
}
