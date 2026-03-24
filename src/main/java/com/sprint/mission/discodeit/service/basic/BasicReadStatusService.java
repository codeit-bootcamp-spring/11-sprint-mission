package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public void createReadStatus(ReadStatusCreateDto statusCreateDto) {
        userRepository.findById(statusCreateDto.authorId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 입니다 👉 " + statusCreateDto.authorId()));
        channelRepository.findById(statusCreateDto.channelId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널 입니다 👉 " + statusCreateDto.channelId()));

        readStatusRepository.findByUserIdAndChannelId(statusCreateDto.authorId(), statusCreateDto.channelId())
                .ifPresent(rs -> {throw new IllegalArgumentException("이미 채널에 대한 읽음 상태가 존재합니다.");});

        ReadStatus readStatus = ReadStatus.builder()
                .id(UUID.randomUUID())
                .userId(statusCreateDto.authorId())
                .channelId(statusCreateDto.channelId())
                .lastReadAt(Instant.now())
                .build();

        readStatusRepository.save(readStatus);

        System.out.println("✅ 읽음 상태 기록 완료 : [유저 : " + statusCreateDto.authorId() + ", 채널 : " + statusCreateDto.channelId());
    }

    public ReadStatus findById(UUID id) {
        return readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 상태 기록이 존재하지 않습니다. ID : " + id));
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID authorId) {
        return readStatusRepository.findAllByUserId(authorId);
    }

    @Override
    public void deleteReadStatus(UUID id) {
        ReadStatus status = findById(id);

        readStatusRepository.deleteById(status.getId());

        System.out.println("✅ 읽음 상태 삭제 완료 : [ID : " + id + "]");
    }

    @Override
    public ReadStatus updateStatus(UUID userId, UUID channelId, ReadStatusUpdateDto request) {
        ReadStatus readStatus = readStatusRepository.findByUserIdAndChannelId(userId, channelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채널의 수신 정보를 찾을 수 없습니다."));

        readStatus.update(request.lastReadAt());

        return readStatusRepository.save(readStatus);
    }
}
