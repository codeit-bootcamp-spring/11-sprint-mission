package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;


    @Override
    public ReadStatusDto.Response create(ReadStatusDto.CreateRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!channelRepository.existsById(request.channelId())) {
            throw new BusinessException(ErrorCode.CHANNEL_NOT_FOUND);
        }

        // 같은 Channel 및 User 관련 객체 존재 시
        boolean isDuplicate = readStatusRepository.findAll().stream()
                .anyMatch(rs -> rs.getUserId().equals(request.userId()) &&
                        rs.getChannelId().equals(request.channelId()));
        if (isDuplicate) {
            throw new BusinessException(ErrorCode.READ_STATUS_ALREADY_EXISTS);
        }

        ReadStatus readStatus = request.toEntity();
        return ReadStatusDto.Response.of(readStatusRepository.save(readStatus));
    }

    @Override
    public ReadStatusDto.Response findById(UUID id) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.READ_STATUS_NOT_FOUND));
        return ReadStatusDto.Response.of(readStatus);
    }

    @Override
    public List<ReadStatusDto.Response> findAllByUserId(UUID userId) {
        return readStatusRepository.findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .map(ReadStatusDto.Response::of)
                .toList();
    }

    @Override
    public ReadStatusDto.Response update(UUID id, ReadStatusDto.UpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.READ_STATUS_NOT_FOUND));

        readStatus.update(Instant.now());

        ReadStatus updatedReadStatus = readStatusRepository.save(readStatus);
        return ReadStatusDto.Response.of(updatedReadStatus);
    }

    @Override
    public void delete(UUID id) {
        if (!readStatusRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.READ_STATUS_NOT_FOUND);
        }
        readStatusRepository.deleteById(id);
    }
}