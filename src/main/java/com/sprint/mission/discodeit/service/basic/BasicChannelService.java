package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channeldto.*;
import com.sprint.mission.discodeit.dto.channeldto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channeldto.request.PublicChanelUpdateRequest;
import com.sprint.mission.discodeit.dto.channeldto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.messagedto.LastMessageTimeDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.base.BaseEntity;
import com.sprint.mission.discodeit.exception.service.channel.NonExistChannelException;
import com.sprint.mission.discodeit.exception.service.channel.WrongChannelTypeException;
import com.sprint.mission.discodeit.exception.service.user.NonExistUserException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import com.sprint.mission.discodeit.repository.JPAReadStatusRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

  private final JPAChannelRepository channelRepository;
  private final JPAReadStatusRepository readStatusRepository;
  private final JPAUserRepository userRepository;
  private final JPAMessageRepository messageRepository;

  private final ChannelMapper channelMapper;


  @Override
  @Transactional
  public ChannelDto createPublic(PublicChannelCreateRequest publicChannelCreateRequest) {

    log.info("공개 채널 생성 요청: {}", publicChannelCreateRequest);

    // 새 채널 생성
    Channel channel = new Channel(

        publicChannelCreateRequest.name(),
        publicChannelCreateRequest.description(),
        Channel.ChannelType.PUBLIC
    );

    channelRepository.save(channel);
    List<User> users = userRepository.findAll();

    //모든 공개 채널에 대하여 기존 유저들에게 readStatus 생성
    for (User user : users) {

      ReadStatus readStatus = new ReadStatus(
          user,
          channel,
          Instant.now()

      );
      readStatusRepository.save(readStatus);

    }

    //채널 참여자 뽑아오기
    List<User> participants = readStatusRepository.findAllByChannel_Id(channel.getId()).stream()
        .map(ReadStatus::getUser)
        .toList();

    //채널의 가장 마지막 메시지 전송 시간
    Instant lastMessageAt = messageRepository.findTopByChannel_IdOrderByCreatedAtDesc(
        channel.getId()).map(BaseEntity::getCreatedAt).orElse(Instant.now());

    log.info("공개 채널 생성 완료! channel : {}", channel);

    return channelMapper.toDto(channel, participants, lastMessageAt);

  }

  @Override
  @Transactional
  public ChannelDto createPrivate(PrivateChannelCreateRequest privateChannelCreateRequest) {

    log.info("개인 채널 생성 : {}", privateChannelCreateRequest);

    //Channel 생성
    Channel channel = new Channel(
        null,
        null,
        ChannelType.PRIVATE
    );

    //readStatus 생성
    privateChannelCreateRequest.participantIds().forEach(userId -> {

      ReadStatus readStatus = new ReadStatus(
          userRepository.findById(userId).orElseThrow(() -> new NonExistUserException(userId)),
          channel,
          Instant.now()
      );
      readStatusRepository.save(readStatus);

    });

    channelRepository.save(channel);

    //채널 참여자 뽑아오기
    List<User> participants = readStatusRepository.findAllByChannel_Id(channel.getId()).stream()
        .map(ReadStatus::getUser)
        .toList();

    //채널의 가장 마지막 메시지 전송 시간
    Instant lastMessageAt = messageRepository.findTopByChannel_IdOrderByCreatedAtDesc(
        channel.getId()).map(BaseEntity::getCreatedAt).orElse(Instant.now());

    log.info("개인 채널 생성 완료. channel : {}", channel);

    return channelMapper.toDto(channel, participants, lastMessageAt);


  }


  @Override
  @Transactional(readOnly = true)
  public List<ChannelDto> findAllByUserId(UUID userId) {

    //유저가 속한 채널을 쿼리 한번에 가져옴.
    List<Channel> channels = channelRepository.findAllByUser_Id(userId);

    List<UUID> channelsId = channels.stream().map(Channel::getId).toList();

    //In 쿼리를 통해 채널 아이디들에 속한 ReadStatus 모조리 가져오기
    List<ReadStatus> readStatuses = readStatusRepository.findAllByChannel_IdIn(channelsId).stream()
        .toList();

    //GroupBy로 나누기
    Map<UUID, List<User>> participantsMap = readStatuses.stream()
        .collect(Collectors.groupingBy((ReadStatus readStatus) -> {
          Channel channel = readStatus.getChannel();
          return channel.getId();
        }, Collectors.mapping(ReadStatus::getUser, Collectors.toList())));

    Map<UUID, Instant> lastMessageTimes = messageRepository.findAllLastMessageAtByChannel_Id(
            channelsId).stream()
        .collect(Collectors.toMap(
            LastMessageTimeDto::channelId,
            LastMessageTimeDto::lastMessageTime
        ));

    return channels.stream()
        .map(channel -> {
          List<User> participants = participantsMap.get(channel.getId());
          Instant lastMessageTime = lastMessageTimes.get(channel.getId());
          return channelMapper.toDto(channel, participants, lastMessageTime);
        })
        .toList();


  }

  @Override
  @Transactional
  public ChannelDto updateChannel(UUID channelId,
      PublicChanelUpdateRequest publicChanelUpdateRequest) {

    log.info("공개 채널 수정, channelId : {}, publicChanelUpdateRequest : {}", channelId,
        publicChanelUpdateRequest);

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new NonExistChannelException(channelId));

    //public check
    if (channel.getType() == Channel.ChannelType.PRIVATE) {
      throw new WrongChannelTypeException(channelId);
    }
    channel.updateName(publicChanelUpdateRequest.newName());
    channel.updateDescription(publicChanelUpdateRequest.newDescription());

    //채널 참여자 뽑아오기
    List<User> participants = readStatusRepository.findAllByChannel_Id(channel.getId()).stream()
        .map(ReadStatus::getUser)
        .toList();

    //채널의 가장 마지막 메시지 전송 시간
    Instant lastMessageAt = messageRepository.findTopByChannel_IdOrderByCreatedAtDesc(
        channel.getId()).map(BaseEntity::getCreatedAt).orElse(null);

    log.info("공개 채널 수정 완료, channel : {}", channel);
    return channelMapper.toDto(channel, participants, lastMessageAt);


  }


  @Override
  @Transactional
  public void deleteChannel(UUID channelId) {

    log.info("채널 삭제 시작, channelId : {}", channelId);

    if (!channelRepository.existsById(channelId)) {
      throw new NonExistChannelException(channelId);
    }

    readStatusRepository.findAllByChannel_Id(channelId)
        .forEach(readStatus -> readStatusRepository.deleteById(readStatus.getId()));

    channelRepository.deleteById(channelId);
    log.info("채널 삭제 완료, channelId : {}", channelId);


  }


}
