package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // 생성자
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;

  // Create
//    @Override
//    public Channel create(Channel.ChannelType channelType, String name, String description) {
//        Channel channel = Channel.create(channelType, name, description);
//        channelRepository.insert(channel);
//        System.out.println("채널을 생성하였습니다.");
//        System.out.println();
//
//        return channel;
//    }
  @Override
  @Transactional
  public Channel createPublic(ChannelCreatePublicRequest dto) {
    Channel channel = Channel.createPublic(dto.name(), dto.description());
    channelRepository.save(channel);

    return channel;
  }

  @Override
  @Transactional
  public Channel createPrivate(ChannelCreatePrivateRequest dto) {
    // 기존 - channel Entity의 participantIds(참여자 Id, List<UUID>)를 사용하여 참여자들을 addAll
    // 수정 - userRepository를 사용하여
    // (클래스 다이어그램에서는 User과 Channel의 참조관계가 무관한데 ChannelService에서 userRepository를 사용해도 괜찮을까요 ?) 꼭 질문하기
    Channel channel = Channel.createPrivate();
    channelRepository.save(channel);

    List<User> users = userRepository.findAllById(
        dto.participantIds()); // 선택된 참여자 id를 전부 찾아 user 리스트에 반환

    // private 채널 참여자들의 ReadStatus 생성
    // readStatusService를 사용하면 같은 레이어(여기서는 Service)간에 순환 참조가 생기므로 readStatusService.create 사용 X
    users.stream()
        .map(user -> new ReadStatus(user, channel, Instant.now()))
        .forEach(readStatusRepository::save);

    return channel;
  }


  // Read
  @Override
  @Transactional(readOnly = true)
  public ChannelDto find(UUID id) {
    Channel channel = channelRepository.findById(id).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 채널입니다. id : " + id)
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
  public List<ChannelDto> findAllByUserId(UUID userId) {
    return channelRepository.findAll().stream()
        .filter(channel -> { // PUBLIC이면 전체 유저가 채널 조회 가능, PRIVATE는 해당 USER가 참여한 채널만 조회 가능
          // PUBLIC
          if (channel.getType() == Channel.ChannelType.PUBLIC) {
            return true;
          }
          // PRIVATE
          return readStatusRepository.findByChannelId(channel.getId()).stream()
              .anyMatch(readStatus -> readStatus.getUser().equals(userId));
        })
        .map(channel -> {
          // 최근 메시지의 시간 조회
          Instant lastMessageAt = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(
                  channel.getId())
              .map(Message::getCreatedAt).orElse(null);

          // PRIVATE 채널일 경우 참여자 포함
          List<User> participants = List.of();
          if (channel.getType() == Channel.ChannelType.PRIVATE) {
            participants = readStatusRepository.findByChannelId(channel.getId()).stream()
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
  public Channel update(UUID id, ChannelUpdateRequest dto) {
    Channel channel = channelRepository.findById(id).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 채널입니다. id " + id)
    );

    // PRIVATE 채널은 수정 불가
    if (channel.getType() == Channel.ChannelType.PRIVATE) {
      throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
    }

    if (dto.newName() != null) {
      channel.updateName(dto.newName());
    }
    if (dto.newDescription() != null) {
      channel.updateDescription(dto.newDescription());
    }
    channelRepository.save(channel);
    System.out.println();

    return channel;
  }

  // Delete
  // 기존 채널만 삭제
  // 고도화 이후 : 채널 내 모든 메시지, 채널의 최근 메시지를 읽은 유저의 시간, 해당 채널 삭제
  @Override
  @Transactional
  public void delete(UUID id) {
    Channel channel = channelRepository.findById(id).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 채널입니다. id" + id)
    );
    messageRepository.deleteAllByChannelId(id);
    readStatusRepository.deleteAllByChannelId(id);
    channelRepository.deleteById(id);
  }
}
