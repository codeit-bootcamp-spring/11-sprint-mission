package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readStatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.readStatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
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
    public void create(ReadStatusCreateRequest request) {
        UUID userId = request.getUserId();
        UUID channelId = request.getChannelId();

        if (userRepository.findById(userId) == null) {
            throw new UserNotFoundException("존재하지 않는 유저입니다. (userId: " + userId + ")");
        }
        if (channelRepository.findById(channelId) == null) {
            throw new ChannelNotFoundException("존재하지 않는 채널입니다. (channelId: " + channelId + ")");
        }

        boolean isExist = readStatusRepository.findAll().stream()
                .anyMatch(rs -> rs.getUserId().equals(userId) && rs.getChannelId().equals(channelId));

        if (isExist) {
            throw new ReadStatusAlreadyExistsException("해당 유저의 이 채널에 대한 ReadStatus가 이미 존재합니다.");
        }

        ReadStatus readStatus = new ReadStatus(userId, channelId);
        readStatusRepository.save(readStatus);
    }

    @Override
    public ReadStatus read(UUID id) {
        ReadStatus readStatus = readStatusRepository.findById(id);
        if (readStatus == null) {
            throw new ReadStatusNotFoundException("조회할 ReadStatus를 찾을 수 없습니다. (ID: " + id + ")");
        }
        return readStatus;
    }

    @Override
    public List<ReadStatus> readAllByUserId(UUID userId) {
        return readStatusRepository.findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .toList();
    }

    @Override
    public void update(UUID id, ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(id);
        if (readStatus == null) {
            throw new ReadStatusNotFoundException("갱신할 읽음 상태를 찾을 수 없습니다. (ID: " + id + ")");
        }

        readStatus.updateTime();

        readStatusRepository.save(readStatus);
    }

    @Override
    public void delete(UUID id) {
        if (readStatusRepository.findById(id) == null) {
            throw new ReadStatusNotFoundException("삭제할 ReadStatus를 찾을 수 없습니다. (ID: " + id + ")");
        }
        readStatusRepository.deleteById(id);
    }
}