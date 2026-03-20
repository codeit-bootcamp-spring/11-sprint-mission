package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.readStatus.CreateReadStatusRequest;
import com.sprint.mission.discodeit.dto.request.readStatus.UpdateReadStatusRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
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
    public ReadStatus createReadStatus(CreateReadStatusRequest request) {
        if (!userRepository.findById(request.getUserId()).isPresent()) {
            throw new IllegalArgumentException("존재하지 않는 User입니다.");
        }
        if (!channelRepository.findById(request.getChannelId()).isPresent()) {
            throw new IllegalArgumentException("존재하지 않는 Channel입니다.");
        }
        if (readStatusRepository.findByUserIdAndChannelId(request.getUserId(), request.getChannelId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 ReadStatus입니다.");
        }
        ReadStatus readStatus = new ReadStatus(request.getUserId(), request.getChannelId());
        readStatusRepository.save(readStatus);
        return readStatus;
    }

    @Override
    public ReadStatus getReadStatusById(UUID id) {
        return readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ReadStatus입니다."));
    }

    @Override
    public List<ReadStatus> getReadStatusesByUserId(UUID userId) {
        return readStatusRepository.findByUserId(userId);
    }

    @Override
    public void updateReadStatus(UUID id, UpdateReadStatusRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ReadStatus입니다."));
        readStatus.update(request.getLastReadAt());
        readStatusRepository.save(readStatus);
    }

    @Override
    public void deleteReadStatus(UUID id) {
        readStatusRepository.deleteById(id);
    }
}