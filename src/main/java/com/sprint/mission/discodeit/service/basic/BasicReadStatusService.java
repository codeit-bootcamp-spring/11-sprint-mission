package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
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
    public ReadStatus create(ReadStatusCreateDto dto) {
        // 관련된 Channel, User가 존재하지 않으면 예외를 발생.
        userRepository.findById(dto.userId());
        channelRepository.findById(dto.channelId());

        // 같은 Channel, User와 관련된 객체가 이미 존재하면 예외를 발생
        readStatusRepository.findByUserId(dto.userId()).stream()
                .filter(readStatus -> readStatus.getChannelId().equals(dto.channelId()))
                .findFirst()
                .ifPresent(readStatus -> {
                    throw new IllegalArgumentException("이미 해당 채널의 읽음 상태가 존재합니다. userId: " + dto.userId() + ", channelId: " + dto.channelId());
                }
                );

        ReadStatus readStatus = new ReadStatus(dto.userId(),dto.channelId(), Instant.now());
        readStatusRepository.insert(readStatus);

        return readStatus;
    }

    @Override
    public ReadStatus find(UUID id) {
        return readStatusRepository.findById(id);
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return readStatusRepository.findByUserId(userId);
    }

    @Override
    public ReadStatus update(UUID id, ReadStatusUpdateDto dto) {
        ReadStatus readStatus = readStatusRepository.findById(id);
        readStatus.updateLastReadAt();
        readStatusRepository.update(readStatus);

        return readStatus;
    }

    @Override
    public void delete(UUID id) {
        readStatusRepository.delete(id);
    }
}
