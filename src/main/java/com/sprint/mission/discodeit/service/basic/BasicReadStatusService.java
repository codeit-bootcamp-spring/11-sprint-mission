package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateParam;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DuplicatedException;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReadStatusMapper readStatusMapper;

    @Override
    @Transactional
    public ReadStatusDto create(ReadStatusCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 채널입니다."));

        boolean alreadyExists = readStatusRepository
                .existsByUser_IdAndChannel_Id(request.userId(), request.channelId());

        if (alreadyExists) {
            throw new DuplicatedException("이미 해당 사용자와 채널의 ReadStatus가 존재합니다.");
        }

        ReadStatus readStatus = new ReadStatus(user, channel, request.lastReadAt());
        ReadStatus savedReadStatus = readStatusRepository.save(readStatus);
        return readStatusMapper.toDto(savedReadStatus);
    }

    @Override
    public Optional<ReadStatusDto> find(UUID id) {
        return readStatusRepository.findById(id).map(readStatusMapper::toDto);
    }

    @Override
    public List<ReadStatusDto> findAllByUserId(UUID userId) {
        return readStatusRepository.findAllByUser_Id(userId).stream()
                .map(readStatusMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ReadStatusDto update(ReadStatusUpdateParam param) {
        ReadStatus readStatus = readStatusRepository.findById(param.id())
                .orElseThrow(() -> new NotFoundException("해당 ReadStatus가 존재하지 않습니다."));

        readStatus.update(param.request().newLastReadAt());
        return readStatusMapper.toDto(readStatus);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        readStatusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 ReadStatus가 존재하지 않습니다."));

        readStatusRepository.deleteById(id);
    }
}