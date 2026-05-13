package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.DuplicateReadStatusException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
  public ReadStatusDto.Response create(ReadStatusDto.CreateRequest request) {
    log.debug("읽음 상태 생성 시작: userId={}, channelId={}", request.userId(), request.channelId());

    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> UserNotFoundException.withId(request.userId()));

    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> ChannelNotFoundException.withId(request.channelId()));

    // 같은 Channel 및 User 관련 객체 존재 시
    if (readStatusRepository.existsByUserIdAndChannelId(request.userId(), request.channelId())) {
      throw DuplicateReadStatusException.withUserIdAndChannelId(request.userId(),
          request.channelId());
    }
    ReadStatus readStatus = request.toEntity(user, channel);
    readStatusRepository.save(readStatus);

    log.info("읽음 상태 생성 완료: readStatusId={}", readStatus.getId());
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  public ReadStatusDto.Response findById(UUID id) {
    log.debug("읽음 상태 단건 조회 시작: id={}", id);

    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> ReadStatusNotFoundException.withId(id));

    log.info("읽음 상태 단건 조회 완료: id={}", id);
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  public List<ReadStatusDto.Response> findAllByUserId(UUID userId) {
    log.debug("사용자 ID 기반 읽음 상태 다건 조회 시작: userId={}", userId);

    List<ReadStatusDto.Response> responses = readStatusRepository.findAllByUserIdWithChannelAndUser(
            userId).stream()
        .map(readStatusMapper::toDto)
        .toList();

    log.info("사용자 ID 기반 읽음 상태 다건 조회 완료: 총 {}건", responses.size());
    return responses;
  }

  @Override
  @Transactional
  public ReadStatusDto.Response update(UUID id, ReadStatusDto.UpdateRequest request) {
    log.debug("읽음 상태 업데이트 시작: id={}", id);

    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> ReadStatusNotFoundException.withId(id));

    Instant newLastReadAt = (request != null && request.newLastReadAt() != null)
        ? request.newLastReadAt()
        : Instant.now();
    readStatus.update(newLastReadAt);

    log.info("읽음 상태 업데이트 완료: id={}", id);
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    log.debug("읽음 상태 삭제 시작: id={}", id);
    
    if (!readStatusRepository.existsById(id)) {
      throw ReadStatusNotFoundException.withId(id);
    }
    readStatusRepository.deleteById(id);
    log.info("읽음 상태 삭제 완료: id={}", id);
  }
}