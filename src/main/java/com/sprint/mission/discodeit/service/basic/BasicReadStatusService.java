package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicReadStatusService implements ReadStatusService {

    private final ChannelRepository channelRepo;
    private final UserRepository userRepo;
    private final ReadStatusRepository readStatusRepo;
    private final ReadStatusMapper readStatusMapper;

    @Override
    @Transactional
    public ReadStatusDto create(ReadStatusCreateRequest dto) {
        Channel channel = channelRepo.findById(dto.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        User user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if(readStatusRepo.findByUserAndChannel(user, channel).isPresent())
            throw new BusinessException(ErrorCode.READ_STATUS_ALREADY_EXISTS);

        ReadStatus readStatus = new ReadStatus(user, channel, dto.lastReadAt());
        readStatusRepo.save(readStatus);
        log.info("ReadStatus created. userId={}, channelId={}", user.getId(), channel.getId());

        return readStatusMapper.toDto(readStatus);
    }

    @Override
    public ReadStatusDto find(UUID id) {
        ReadStatus readStatus = readStatusRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.READ_STATUS_NOT_FOUND));

        return readStatusMapper.toDto(readStatus);
    }

    @Override
    public List<ReadStatusDto> findAllByUserId(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return readStatusRepo.findAllByUser(user).stream()
                .map(readStatusMapper::toDto)
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
        log.info("ReadStatus deleted. userId={}, channelId={}",
                readStatus.getUser().getId(), readStatus.getChannel().getId());
    }
}
