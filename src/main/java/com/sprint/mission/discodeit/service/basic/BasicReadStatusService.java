package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final ChannelRepository channelRepo;
    private final UserRepository userRepo;
    private final ReadStatusRepository readStatusRepo;

    @Override
    @Transactional
    public ReadStatusDto create(ReadStatusCreateRequest dto) {
        Channel channel = channelRepo.findById(dto.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        User user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if(readStatusRepo.findByUserIdAndChannelId(user.getId(), channel.getId()).isPresent())
            throw new BusinessException(ErrorCode.READ_STATUS_ALREADY_EXISTS);

        ReadStatus readStatus = new ReadStatus(user, channel, dto.lastReadAt());
        readStatusRepo.save(readStatus);

        return toDto(readStatus);
    }

    @Override
    public ReadStatusDto find(UUID id) {
        ReadStatus readStatus = readStatusRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.READ_STATUS_NOT_FOUND));

        return toDto(readStatus);
    }

    @Override
    public List<ReadStatusDto> findAllByUserId(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return readStatusRepo.findAllByUser(user).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void update(UUID readStatusId, ReadStatusUpdateRequest dto) {
        ReadStatus readStatus = readStatusRepo.findById(readStatusId)
                .orElseThrow(() -> new BusinessException(ErrorCode.READ_STATUS_NOT_FOUND));

        readStatus.update(dto.newLastReadAt());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ReadStatus readStatus = readStatusRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.READ_STATUS_NOT_FOUND));
        readStatusRepo.delete(readStatus);
    }

    private ReadStatusDto toDto(ReadStatus readStatus) {
        return new ReadStatusDto(
                readStatus.getId(),
                readStatus.getCreatedAt(),
                readStatus.getUpdatedAt(),
                readStatus.getUser().getId(),
                readStatus.getChannel().getId(),
                readStatus.getLastReadAt()
        );
    }
}
