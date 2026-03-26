package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ReadStatusResponse create(ReadStatusCreateRequest request) {
        // User 존재 검증
        if (userRepository.findById(request.userId()) == null) {
            throw new IllegalStateException("존재하지 않는 유저입니다.");
        }

        // Channel 존재 검증
        if (channelRepository.findById(request.channelId()) == null) {
            throw new IllegalStateException("존재하지 않는 채널입니다.");
        }

        // 같은 유저 + 채널 중복 검증
        if (readStatusRepository.findByUserIdAndChannelId(request.userId(), request.channelId()) != null) {
            throw new IllegalStateException("이미 해당 유저와 채널의 ReadStatus가 존재합니다.");
        }

        ReadStatus readStatus = new ReadStatus(request.userId(), request.channelId());
        ReadStatus savedReadStatus = readStatusRepository.save(readStatus);

        return ReadStatusResponse.of(savedReadStatus);
    }

    @Override
    public ReadStatusResponse findById(UUID id) {
        ReadStatus readStatus = readStatusRepository.findById(id);

        if (readStatus == null) {
            throw new IllegalStateException("존재하지 않는 ReadStatus입니다.");
        }

        return ReadStatusResponse.of(readStatus);
    }

    @Override
    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        if (userRepository.findById(userId) == null) {
            throw new IllegalStateException("존재하지 않는 유저입니다.");
        }

        return readStatusRepository.findByUserId(userId).stream()
                .map(ReadStatusResponse::of)
                .toList();
    }

    @Override
    public ReadStatusResponse update(UUID id, ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(id);
        if (readStatus == null) {
            throw new IllegalStateException("존재하지 않는 ReadStatus입니다.");
        }

        // lastReadAt 수정
        readStatus.updateLastReadAt(request.lastReadAt());

        ReadStatus updatedReadStatus = readStatusRepository.save(readStatus);
        return ReadStatusResponse.of(updatedReadStatus);
    }

    @Override
    public void delete(UUID id) {
        if (readStatusRepository.findById(id) == null) {
            throw new IllegalStateException("존재하지 않는 ReadStatus입니다.");
        }
        readStatusRepository.delete(id);
    }
}
