package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channeldto.*;
import com.sprint.mission.discodeit.dto.channeldto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channeldto.request.PublicChanelUpdateRequest;
import com.sprint.mission.discodeit.dto.channeldto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.exception.service.WrongChannelTypeException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JPAReadStatusRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

  private final JPAChannelRepository channelRepository;
  private final JPAReadStatusRepository readStatusRepository;
  private final JPAUserRepository userRepository;

  private final ChannelMapper channelMapper;


  @Override
  @Transactional
  public ChannelDto createPublic(PublicChannelCreateRequest publicChannelCreateRequest) {

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

    return channelMapper.toDto(channel);

  }

  @Override
  @Transactional
  public ChannelDto createPrivate(PrivateChannelCreateRequest privateChannelCreateRequest) {

    Channel channel = new Channel(
        null,
        null,
        ChannelType.PRIVATE
    );

    //readStatus 생성
    privateChannelCreateRequest.participantIds().forEach(userId -> {

      if (!userRepository.existsById(userId)) {
        throw new NonExistException("존재하지 않는 유저 아이디입니다.");
      }
      channelRepository.save(channel);

      ReadStatus readStatus = new ReadStatus(
          userRepository.findById(userId).orElseThrow(),
          channel,
          Instant.now()
      );
      readStatusRepository.save(readStatus);

    });

    return channelMapper.toDto(channel);


  }


  @Override
  @Transactional(readOnly = true)
  public List<ChannelDto> findAllByUserId(UUID userId) {

    return channelRepository.findAllByUser_Id(userId).stream()
        .map(channelMapper::toDto)
        .toList();


  }

  @Override
  @Transactional
  public ChannelDto updateChannel(UUID channelId,
      PublicChanelUpdateRequest publicChanelUpdateRequest) {

    Channel channel = channelRepository.findById(channelId).orElseThrow();

    //public check
    if (channel.getType() == Channel.ChannelType.PRIVATE) {
      throw new WrongChannelTypeException("Private 타입 채널은 변경할 수 없습니다.");
    }
    channel.updateName(publicChanelUpdateRequest.newName());
    channel.updateDescription(publicChanelUpdateRequest.newDescription());

    return channelMapper.toDto(channel);


  }


  @Override
  @Transactional
  public void deleteChannel(UUID channelId) {

    if (!channelRepository.existsById(channelId)) {
      throw new NonExistException("존재하는 채널이 아닙니다.");
    }

    readStatusRepository.findAllByChannel_Id(channelId)
        .forEach(readStatus -> readStatusRepository.deleteById(readStatus.getId()));

    channelRepository.deleteById(channelId);


  }


}
