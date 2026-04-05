package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channeldto.*;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.exception.service.WrongChannelTypeException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;


  @Override
  public CreatedChannelInfo createPublic(CreatePublicChannel createPublicChannel) {

    Channel channel = new Channel(

        createPublicChannel.name(),
        Channel.ChannelType.PUBLIC,
        createPublicChannel.description()
    );

    userRepository.getAllUser().forEach(user -> {

      readStatusRepository.save(
          new ReadStatus(user.getId(), channel.getId(), Instant.now().minusSeconds(1)));


    });

    //default Message
    messageRepository.saveMessage(new Message(
        null,
        channel.getId(),
        "default message",
        new ArrayList<>()

    ));

    channelRepository.saveChannel(channel);
    return channelToCreatedInfo(channel);


  }

  @Override
  public CreatedChannelInfo createPrivate(CreatePrivateChannel createPrivateChannel) {

    Channel channel = new Channel(
        null,
        Channel.ChannelType.PRIVATE,
        null
    );

    //readStatus 생성
    createPrivateChannel.participantIds().forEach(userId -> {

      userRepository.getUser(userId).orElseThrow(() -> new NonExistException("존재하지 않는 유저입니다."));
      readStatusRepository.save(
          new ReadStatus(userId, channel.getId(), Instant.now().minusSeconds(100)));

    });

    //default Message
    messageRepository.saveMessage(new Message(

        null,
        channel.getId(),
        "default message",
        new ArrayList<>()

    ));

    channelRepository.saveChannel(channel);

    return channelToCreatedInfo(channel);


  }


  @Override
  public ChannelInfo findPublic(UUID channelId) {

    return channelToDto(channelRepository.getChannel(channelId).orElseThrow());

  }


  @Override
  public ChannelInfo findPrivate(UUID channelId, UUID memberId) {

    channelRepository.getChannel(channelId).orElseThrow();

    if (!readStatusRepository.isExist(memberId, channelId)) {
      throw new NonExistException("채널에 해당 유저가 존재하지 않습니다.");
    }

    return channelToDto(channelRepository.getChannel(channelId).orElseThrow());

  }


  @Override
  public List<ChannelInfo> findAllById(UUID userId) {

    return channelRepository.getAllChannel().stream()
        .filter(channel -> {

          ReadStatus readStatus = readStatusRepository.get(userId, channel.getId()).orElse(null);

          return (readStatus != null) && ((channel.getChannelType() == Channel.ChannelType.PRIVATE)
              || channel.getChannelType() == Channel.ChannelType.PUBLIC);

        })
        .map(this::channelToDto)
        .toList();

  }

  @Override
  public CreatedChannelInfo updateChannel(UUID channelId, UpdateChannel updateChannel) {

    Channel channel = channelRepository.getChannel(channelId).orElseThrow();

    //public check
    if (channel.getChannelType() == Channel.ChannelType.PRIVATE) {
      throw new WrongChannelTypeException("Private 타입 채널은 변경할 수 없습니다.");
    }
    channel.updateChannelName(updateChannel.newName());
    channel.updateChannelDescription(updateChannel.newDescription());

    channelRepository.saveChannel(channel);

    return channelToCreatedInfo(channel);


  }


  @Override
  public void deleteChannel(UUID channelId) {

    if (!channelRepository.isExistChannel(channelId)) {
      throw new NonExistException("존재하는 채널이 아닙니다.");
    }

    readStatusRepository.getAllByChannelId(channelId)
        .forEach(readStatus -> readStatusRepository.delete(readStatus.getUserId(),
            channelId));

    channelRepository.deleteChannel(channelId);


  }

  public ChannelInfo channelToDto(Channel channel) {

    return new ChannelInfo(

        channel.getId(),
        channel.getChannelType(),
        channel.getChannelName(),
        channel.getChannelDescription(),
        readStatusRepository.getAllByChannelId(channel.getId()).stream()
            .map(ReadStatus::getUserId).toList(),
        messageRepository.getLastMessagebyChannelId(channel.getId())
            .map(Message::getCreatedAt).orElseThrow()

    );

  }

  public CreatedChannelInfo channelToCreatedInfo(Channel channel) {

    return new CreatedChannelInfo(

        channel.getId(),
        channel.getCreatedAt(),
        channel.getUpdatedAt(),
        channel.getChannelType(),
        channel.getChannelName(),
        channel.getChannelDescription()
    );


  }


}
