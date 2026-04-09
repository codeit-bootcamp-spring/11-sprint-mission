package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  @Transactional
  public MessageResponse create(MessageCreateRequest request) {
    User author = userRepository.findById(request.getAuthorId())
        .orElseThrow(() -> DiscodeitNotFoundException.user(request.getAuthorId()));

    Channel channel = channelRepository.findById(request.getChannelId())
        .orElseThrow(() -> DiscodeitNotFoundException.channel(request.getChannelId()));

    Message message = new Message(request.getContent(), channel, author);
    Message savedMessage = messageRepository.save(message);

    return new MessageResponse(
        savedMessage.getId(),
        savedMessage.getContent(),
        savedMessage.getChannel().getId(),
        savedMessage.getAuthor().getId()
    );
  }

  @Override
  @Transactional(readOnly = true)
  public MessageResponse find(UUID id) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.message(id));

    return new MessageResponse(
        message.getId(),
        message.getContent(),
        message.getChannel().getId(),
        message.getAuthor().getId()
    );
  }

  @Override
  @Transactional(readOnly = true)
  public List<MessageResponse> findAllByChannelId(UUID channelId) {
    channelRepository.findById(channelId)
        .orElseThrow(() -> DiscodeitNotFoundException.channel(channelId));

    return messageRepository.findAllByChannel_Id(channelId).stream()
        .map(message -> new MessageResponse(
            message.getId(),
            message.getContent(),
            message.getChannel().getId(),
            message.getAuthor().getId()
        ))
        .toList();
  }

  @Override
  @Transactional
  public void update(MessageUpdateRequest request) {
    Message message = messageRepository.findById(request.getMessageId())
        .orElseThrow(() -> DiscodeitNotFoundException.message(request.getMessageId()));

    message.updateMessage(request.getNewContent());
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.message(id));

    messageRepository.delete(message);
  }
}