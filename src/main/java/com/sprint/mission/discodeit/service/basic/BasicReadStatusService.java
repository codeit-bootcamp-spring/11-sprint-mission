package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateParam;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ReadStatusDto create(ReadStatusCreateRequest request) {
        if (userRepository.findById(request.userId()).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        if (channelRepository.findById(request.channelId()).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        boolean alreadyExists = readStatusRepository.findAll().stream()
                .anyMatch(readStatus ->
                        readStatus.getUserId().equals(request.userId()) &&
                                readStatus.getChannelId().equals(request.channelId())
                );

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 해당 사용자와 채널의 ReadStatus가 존재합니다.");
        }

        ReadStatus readStatus = new ReadStatus(
                request.userId(),
                request.channelId(),
                request.lastReadAt()
        );

        ReadStatus savedReadStatus = readStatusRepository.save(readStatus);

        return new ReadStatusDto(
                savedReadStatus.getId(),
                savedReadStatus.getUserId(),
                savedReadStatus.getChannelId(),
                savedReadStatus.getLastReadAt(),
                savedReadStatus.getCreatedAt(),
                savedReadStatus.getUpdatedAt()
        );
    }

    @Override
    public Optional<ReadStatusDto> find(UUID id) {
        Optional<ReadStatus> optionalReadStatus = readStatusRepository.findById(id);

        if (optionalReadStatus.isEmpty()) {
            return Optional.empty();
        }

        ReadStatus readStatus = optionalReadStatus.get();

        ReadStatusDto readStatusDto = new ReadStatusDto(
                readStatus.getId(),
                readStatus.getUserId(),
                readStatus.getChannelId(),
                readStatus.getLastReadAt(),
                readStatus.getCreatedAt(),
                readStatus.getUpdatedAt()
        );

        return Optional.of(readStatusDto);
    }

    @Override
    public List<ReadStatusDto> findAllByUserId(UUID userId) {
        return readStatusRepository.findAll().stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId))
                .map(readStatus -> new ReadStatusDto(
                        readStatus.getId(),
                        readStatus.getUserId(),
                        readStatus.getChannelId(),
                        readStatus.getLastReadAt(),
                        readStatus.getCreatedAt(),
                        readStatus.getUpdatedAt()
                ))
                .toList();
    }

    @Override
    public ReadStatusDto update(ReadStatusUpdateParam param) {
        ReadStatus readStatus = readStatusRepository.findById(param.id())
                .orElseThrow(() -> new IllegalArgumentException("해당 ReadStatus가 존재하지 않습니다."));

        readStatus.update(param.request().lastReadAt());

        ReadStatus savedReadStatus = readStatusRepository.save(readStatus);

        return new ReadStatusDto(
                savedReadStatus.getId(),
                savedReadStatus.getUserId(),
                savedReadStatus.getChannelId(),
                savedReadStatus.getLastReadAt(),
                savedReadStatus.getCreatedAt(),
                savedReadStatus.getUpdatedAt()
        );
    }

    @Override
    public void delete(UUID id) {
        readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ReadStatus가 존재하지 않습니다."));

        readStatusRepository.delete(id);
    }
}