package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor // 생성자
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;

  private final CacheManager cacheManager;

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(value = "userChannels", allEntries = true) // userChannels 캐시의 모든 키들을 제거
  // 예상되는 문제점 : Public Channel 하나만 생성되도 userChannels 캐시가 전부 삭제
  // 실무적인 관점에서는 userChannels를 publicChannels, userPrivateChannels로 캐시 데이터를 분리하고
  // 채널 목록 조회 메서드 findAllByUserId도 public/private 채널별 목록 조회 메서드로 분리
  // 컨트롤러에서는 프론트에서 정상적으로 호출할 수 있도록 public 목록 조회 메서드 + private 목록 조회 메서드를 호출
  public ChannelDto createPublic(ChannelCreatePublicRequest dto) {
    log.debug("[CHANNEL_CREATE_PUBLIC_START] PUBLIC 채널 생성 시작 - 생성할 채널 이름={}, 생성할 채널 설명={}",
        dto.name(), dto.description());

    Channel channel = Channel.createPublic(dto.name(), dto.description());
    channelRepository.save(channel);
    log.info("[CHANNEL_CREATE_PUBLIC_SUCCESS] PUBLIC 채널 생성 완료 - 채널 ID={}, 채널 이름={}",
        channel.getId(), channel.getName());

    return channelMapper.toDto(channel, List.of(), null);
  }

  @Override
  @Transactional
  public ChannelDto createPrivate(ChannelCreatePrivateRequest dto) {
    // 기존 - channel Entity의 participantIds(참여자 Id, List<UUID>)를 사용하여 참여자들을 addAll
    // 수정 - userRepository를 사용
    log.debug("[CHANNEL_CREATE_PRIVATE_START] PRIVATE 채널 생성 시작 - 참여자 수={}, 참여자 ID 목록={}",
        dto.participantIds().size(), dto.participantIds());

    Channel channel = Channel.createPrivate();
    channelRepository.save(channel);

    // 선택된 참여자 id를 전부 찾아 user 리스트에 반환
    List<User> users = userRepository.findAllById(dto.participantIds());

    // private 채널 참여자들의 ReadStatus 생성
    // readStatusService를 사용하면 같은 레이어(여기서는 Service)간에 순환 참조가 생기므로 readStatusService.create 사용 X
    users.stream()
        .map(user -> new ReadStatus(user, channel, Instant.now()))
        .forEach(readStatusRepository::save);

    log.info("[CHANNEL_CREATE_PRIVATE_SUCCESS] PRIVATE 채널 생성 완료 - 채널 ID={}, 참여자 수={}",
        channel.getId(), users.size());

    // userChannels 캐시 데이터 조회
    Cache cache = cacheManager.getCache("userChannels");

    // 비어있지 않다면 evict를 통해 해당 참여자들의 userChannels 캐시 제거
    if (cache != null) {
      dto.participantIds().forEach(cache::evict); // (UUID) id -> cache.evict(id)

      log.debug("[CHANNEL_CREATE_PRIVATE_CACHE_EVICT] PRIVATE 채널 생성 후"
              + " 참여자들의 userChannels 캐시 제거 - 참여자 수={}, 참여자 ID 목록={}",
          dto.participantIds().size(), dto.participantIds());
    }

    return channelMapper.toDto(channel, users, null);
  }


  // Read
  @Override
  @Transactional(readOnly = true)
  public ChannelDto find(UUID id) {
    Channel channel = channelRepository.findById(id).orElseThrow(
        () -> new ChannelNotFoundException(id)
    );

    // 가장 최근 메시지 시간을 조회
    Instant lastMessageAt = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(
            channel.getId())
        .map(Message::getCreatedAt).orElse(null);

    // PRIVATE 채널일 경우 참여자 포함
    List<User> participants = List.of();
    if (channel.getType() == Channel.ChannelType.PRIVATE) {
      participants = readStatusRepository.findByChannelId(id).stream()
          .map(ReadStatus::getUser)
          .toList();
    }
    return channelMapper.toDto(channel, participants, lastMessageAt);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "userChannels", key = "#userId") // 사용자별 채널 목록 조회, (value = cacheNames)
  public List<ChannelDto> findAllByUserId(UUID userId) {

    List<Channel> channels = channelRepository.findAll();
    List<UUID> channelIds = channels.stream().map(Channel::getId).toList();

    List<ReadStatus> readStatuses = readStatusRepository.findByChannelIdIn(channelIds);

    Map<UUID, List<ReadStatus>> readStatusMap = readStatuses.stream().collect(
        Collectors.groupingBy(rs -> rs.getChannel().getId()));

    List<Message> latestMessages = messageRepository.findLastMessagesByChannelIds(channelIds);

    Map<UUID, Instant> lastMessageMap = latestMessages.stream()
        .collect(Collectors.toMap(
            message -> message.getChannel().getId(),
            Message::getCreatedAt,
            (a, b) -> a.isAfter(b) ? a : b));

    return channels.stream()
        .filter(channel -> { // PUBLIC이면 전체 유저가 채널 조회 가능, PRIVATE는 해당 USER가 참여한 채널만 조회 가능
          // PUBLIC
          if (channel.getType() == Channel.ChannelType.PUBLIC) {
            return true;
          }
          // PRIVATE
          return readStatusMap.getOrDefault(channel.getId(), List.of()).stream()
              .anyMatch(readStatus -> readStatus.getUser().getId().equals(userId));
        })
        .map(channel -> {
          // 최근 메시지의 시간 조회
          Instant lastMessageAt = lastMessageMap.get(channel.getId());

          // PRIVATE 채널일 경우 참여자 포함
          List<User> participants = List.of();
          if (channel.getType() == Channel.ChannelType.PRIVATE) {
            participants = readStatusMap.getOrDefault(channel.getId(), List.of()).stream()
                .map(ReadStatus::getUser)
                .toList();
          }

          return channelMapper.toDto(channel, participants, lastMessageAt);
        })
        .toList();
  }


  // Update
  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(value = "userChannels", allEntries = true) // Public 채널만 수정하기 때문에 manager 사용X
  public ChannelDto update(UUID id, ChannelUpdateRequest dto) {
    log.debug("[CHANNEL_UPDATE_START] 채널 수정 시작 - 수정할 채널 ID={}, 요청한 채널 이름={}, 요청한 채널 설명={}",
        id, dto.newName(), dto.newDescription());

    Channel channel = channelRepository.findById(id).orElseThrow(
        () -> {
          log.warn("[CHANNEL_UPDATE_FAILED] 채널 수정 실패 - 존재하지 않음 - 수정할 채널 ID={}", id);
          return new ChannelNotFoundException(id);
        }
    );

    // PRIVATE 채널은 수정 불가
    if (channel.getType() == Channel.ChannelType.PRIVATE) {
      log.warn("[CHANNEL_UPDATE_FAILED] 채널 수정 실패 - PRIVATE 채널 수정 불가 - 수정할 채널 ID={}, 수정할 채널 타입={}",
          id, channel.getType());
      throw new PrivateChannelUpdateException(id);
    }

    if (dto.newName() != null) {
      channel.updateName(dto.newName());
    }
    if (dto.newDescription() != null) {
      channel.updateDescription(dto.newDescription());
    }

    channelRepository.save(channel);

    log.info("[CHANNEL_UPDATE_SUCCESS] 채널 수정 완료 - 수정한 채널 ID={}", id);

    return channelMapper.toDto(channel, List.of(), null);
  }

  // Delete
  // 기존 채널만 삭제
  // 고도화 이후 : 채널 내 모든 메시지, 채널의 최근 메시지를 읽은 유저의 시간, 해당 채널 삭제
  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  public void delete(UUID id) {
    log.debug("[CHANNEL_DELETE_START] 채널 삭제 시작 - 채널 ID={}", id);

    Channel channel = channelRepository.findById(id).orElseThrow(
        () -> {
          log.warn("[CHANNEL_DELETE_FAILED] 채널 삭제 실패 - 존재하지 않음 - 채널 ID={}", id);
          return new ChannelNotFoundException(id);
        }
    );

    Cache cache = cacheManager.getCache("userChannels");

    if (cache != null) {
      // Private Channel일 경우
      if (channel.getType() == ChannelType.PRIVATE) {
        // ReadStatus → User → UUID
        readStatusRepository.findByChannelId(id).stream()
            .map(ReadStatus::getUser)
            .map(User::getId)
            .forEach(cache::evict); // id -> cache.evict(id)
      }
      // Public Channel일 경우
      cache.clear();
    }

    messageRepository.deleteAllByChannelId(id);
    readStatusRepository.deleteAllByChannelId(id);
    channelRepository.deleteById(id);

    log.info("[CHANNEL_DELETE_SUCCESS] 채널 삭제 완료 - 채널 ID={}", id);
  }
}
