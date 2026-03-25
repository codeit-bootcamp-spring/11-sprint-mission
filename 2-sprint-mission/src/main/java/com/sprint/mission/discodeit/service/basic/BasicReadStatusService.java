package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
            throw new NoSuchElementException("User not found with id: " + request.userId());
        }
        if (!channelRepository.existsById(request.channelId())) {
            throw new NoSuchElementException("Channel not found with id: " + request.channelId());
        }

        // 같은 Channel 및 User 관련 객체 존재 시
        boolean isDuplicate = readStatusRepository.findAll().stream()
                .anyMatch(rs -> rs.getUserId().equals(request.userId()) &&
                        rs.getChannelId().equals(request.channelId()));
        if (isDuplicate) {
            throw new IllegalStateException("ReadStatus already exists for this user and channel");
        }

        ReadStatus readStatus = request.toEntity();
        return ReadStatusDto.Response.of(readStatusRepository.save(readStatus));
    }

    @Override
    public ReadStatusDto.Response findById(UUID id) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ReadStatus not found with id: " + id));
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
                .orElseThrow(() -> new NoSuchElementException("ReadStatus not found with id: " + id));

        readStatus.updateLastReadAt();

        ReadStatus updatedReadStatus = readStatusRepository.save(readStatus);
        return ReadStatusDto.Response.of(updatedReadStatus);
    }

    @Override
    public void delete(UUID id) {
        if (!readStatusRepository.existsById(id)) {
            throw new NoSuchElementException("ReadStatus not found with id: " + id);
        }
        readStatusRepository.deleteById(id);
    }
}