package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return readStatusRepository.findByUserId(userId);
    }

    @Override
    public ReadStatus createReadStatus(ReadStatusCreateRequest request) {
        userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        channelRepository.findById(request.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        boolean alreadyExists = readStatusRepository.findByUserId(request.userId()).stream()
                .anyMatch(rs -> rs.getChannelId().equals(request.channelId()));
        if (alreadyExists) {
            throw new BusinessException(ErrorCode.READ_STATUS_ALREADY_EXIST);
        }

        ReadStatus newReadStatus = ReadStatus.create(request.userId(), request.channelId());

        return readStatusRepository.save(newReadStatus);
    }

    @Override
    public ReadStatus updateReadStatus(UUID requestUserId, UUID readStatusId, ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new BusinessException(ErrorCode.READ_STATUS_NOT_FOUND));

        if (!readStatus.getUserId().equals(requestUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        readStatus.updateReadAt();

        return readStatusRepository.save(readStatus);
    }
}