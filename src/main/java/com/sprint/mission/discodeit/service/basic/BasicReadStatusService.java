package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicReadStatusService implements ReadStatusService {
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ReadStatusResponse createReadStatus(ReadStatusCreateRequest readStatusCreateRequest) {
        User user = this.userRepository.findById(readStatusCreateRequest.userId());
        Channel channel = this.channelRepository.findById(readStatusCreateRequest.channelId());

        if (this.readStatusRepository.existByUserIdAndChannelId(user.getId(), channel.getId())) {
            throw new IllegalArgumentException("read status has same channel id and user id already exist. ❌");
        }

        ReadStatus readStatus = new ReadStatus(user, channel);
        this.readStatusRepository.save(readStatus);

        log.info("ReadStatus has been created successfully. ✅ [ID: {}]", readStatus.getId());
        log.info("-> {user: {}, channel: {}}", user.getId(), channel.getId());
        return readStatus.toResponse();
    }

    @Override
    public ReadStatusResponse findById(UUID id) {
        ReadStatus readStatus = this.readStatusRepository.findById(id);
        return readStatus.toResponse();
    }

    @Override
    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        return this.readStatusRepository.findAllByUserId(userId).stream()
                .map(ReadStatus::toResponse)
                .toList();
    }

    @Override
    public ReadStatusResponse updateReadStatus(ReadStatusUpdateRequest readStatusUpdateRequest) {
        ReadStatus readStatus = this.readStatusRepository.findById(readStatusUpdateRequest.id());
        readStatus.setUpdatedAt();
        this.readStatusRepository.save(readStatus);

        log.info("ReadStatus has been updated successfully. ✅ [ID: {}]", readStatus.getId());
        return readStatus.toResponse();
    }

    @Override
    public void deleteReadStatus(UUID id) {
        ReadStatus readStatus = this.readStatusRepository.findById(id);

        this.readStatusRepository.delete(readStatus);

        log.info("ReadStatus has been deleted successfully. ✅ [ID: {}]", readStatus.getId());
    }
}
