package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequestDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponseDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
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

    private final ChannelRepository channelRepo;
    private final UserRepository userRepo;
    private final ReadStatusRepository readStatusRepo;

    @Override
    public ReadStatusResponseDto create(ReadStatusCreateRequestDto dto) {
        channelRepo.findById(dto.channelId())
                .orElseThrow(() -> new ChannelNotFoundException(dto.channelId()));

        userRepo.findById(dto.userId())
                .orElseThrow(() -> new UserNotFoundException(dto.userId()));

        boolean exists = readStatusRepo.findAll().stream()
                .anyMatch(p -> p.getChannelId().equals(dto.channelId())
                && p.getUserId().equals(dto.userId()));
        if(exists) throw new ReadStatusAlreadyExistsException(dto.userId(), dto.channelId());

        ReadStatus readStatus = new ReadStatus(dto.userId(), dto.channelId());
        readStatusRepo.save(readStatus);
        return toDto(readStatus);
    }

    @Override
    public ReadStatusResponseDto find(UUID id) {
        ReadStatus readStatus = readStatusRepo.findById(id)
                .orElseThrow(() -> new ReadStatusNotFoundException(id));

        return toDto(readStatus);
    }

    @Override
    public List<ReadStatusResponseDto> findAllByUserId(UUID id) {
        userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return readStatusRepo.findAll().stream()
                .filter(p -> p.getUserId().equals(id))
                .map(this::toDto)
                .toList();
    }

    @Override
    public void update(UUID id) {
        ReadStatus readStatus = readStatusRepo.findById(id)
                .orElseThrow(() -> new ReadStatusNotFoundException(id));

        readStatus.update();
        readStatusRepo.save(readStatus);
    }

    @Override
    public void delete(UUID id) {
        ReadStatus readStatus = readStatusRepo.findById(id)
                .orElseThrow(() -> new ReadStatusNotFoundException(id));

        readStatusRepo.delete(readStatus);
    }

    private ReadStatusResponseDto toDto(ReadStatus readStatus) {
        return new ReadStatusResponseDto(
                readStatus.getId(), readStatus.getUserId(), readStatus.getChannelId(), readStatus.getUpdatedAt());
    }
}
