package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
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
  public ReadStatus createReadStatus(ReadStatusCreateDto statusCreateDto) {
    userRepository.findById(statusCreateDto.userId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    channelRepository.findById(statusCreateDto.channelId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHANNEL_NOT_FOUND));

    readStatusRepository.findByUserIdAndChannelId(statusCreateDto.userId(),
            statusCreateDto.channelId())
        .ifPresent(rs -> {
          throw new BusinessLogicException(ExceptionCode.READ_STATUS_EXISTS);
        });

    ReadStatus readStatus = ReadStatus.builder()
        .id(UUID.randomUUID())
        .userId(statusCreateDto.userId())
        .channelId(statusCreateDto.channelId())
        .lastReadAt(Instant.now())
        .build();

    readStatusRepository.save(readStatus);

    System.out.println("✅ 읽음 상태 기록 완료 : [유저 : " + statusCreateDto.userId() + ", 채널 : "
        + statusCreateDto.channelId());

    return readStatus;
  }

  public ReadStatus findById(UUID id) {
    return readStatusRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.READ_STATUS_NOT_FOUND));
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
  public ReadStatus updateStatus(UUID readStatusId, ReadStatusUpdateDto request) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.READ_STATUS_NOT_FOUND));

    readStatus.update(request.newLastReadAt());

    return readStatusRepository.save(readStatus);
  }
}
