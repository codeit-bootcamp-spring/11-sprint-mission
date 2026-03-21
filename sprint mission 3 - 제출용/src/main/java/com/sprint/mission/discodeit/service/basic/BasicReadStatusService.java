package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ReadStatus create(ReadStatusCreateRequest request) {
        if (userRepository.read(request.getUserId()) == null) {
            throw DiscodeitException.userNotFound(request.getUserId());
        }

        if (channelRepository.read(request.getChannelId()) == null) {
            throw DiscodeitException.channelNotFound(request.getChannelId());
        }

        if (readStatusRepository.readByUserIdAndChannelId(request.getUserId(), request.getChannelId()) != null) {
            throw DiscodeitException.duplicateReadStatus(request.getUserId(), request.getChannelId());
        }

        ReadStatus readStatus = new ReadStatus(request.getUserId(), request.getChannelId(), Instant.now());
        return readStatusRepository.create(readStatus);
    }

    @Override
    public ReadStatus read(UUID id) {   // ReadStatus는 자체 UUID 없음.
        throw new UnsupportedOperationException("ReadStatus는 id로 조회할 수 없습니다.");
    }

    @Override
    public List<ReadStatus> readAllByUserId(UUID userId) {
        return readStatusRepository.readAllByChannelId(userId);
    }

    @Override
    public void update(ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.readByUserIdAndChannelId(request.getUserId(), request.getChannelId());
        if (readStatus == null) {
            throw DiscodeitException.readStatusNotFound(request.getUserId(), request.getChannelId());
        }
        readStatus.updateLastMessageReadAt(request.getLastMessageReadAt());
        readStatusRepository.update(request.getUserId(), request.getChannelId(), request.getLastMessageReadAt());
    }

    @Override
    public void delete(UUID id) {
        readStatusRepository.deleteByChannelId(id);
    }
}