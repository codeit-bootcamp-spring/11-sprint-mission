package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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

    @Override
    public ReadStatus createReadStatus(UUID userId, UUID channelId) {
        ReadStatus readStatus = new ReadStatus(userId, channelId);
        readStatusRepository.save(readStatus);
        return readStatus;
    }

    @Override
    public ReadStatus getReadStatusById(UUID id) {
        return readStatusRepository.findById(id);
    }

    @Override
    public ReadStatus getReadStatusByUserIdAndChannelId(UUID userId, UUID channelId) {
        return readStatusRepository.findByUserIdAndChannelId(userId, channelId);
    }

    @Override
    public List<ReadStatus> getReadStatusesByUserId(UUID userId) {
        return readStatusRepository.findByUserId(userId);
    }

    @Override
    public List<ReadStatus> getReadStatusesByChannelId(UUID channelId) {
        return readStatusRepository.findByChannelId(channelId);
    }

    @Override
    public List<ReadStatus> getAllReadStatuses() {
        return readStatusRepository.findAll();
    }

    @Override
    public void updateReadStatus(UUID id, Instant lastReadAt) {
        ReadStatus readStatus = readStatusRepository.findById(id);
        if (readStatus != null) {
            readStatus.update(lastReadAt);
            readStatusRepository.save(readStatus);
        }
    }

    @Override
    public void deleteReadStatus(UUID id) {
        readStatusRepository.deleteById(id);
    }
}
