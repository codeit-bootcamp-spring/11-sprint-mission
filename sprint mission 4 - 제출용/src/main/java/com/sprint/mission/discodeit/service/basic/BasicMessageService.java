package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
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

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  public Message create(MessageCreateRequest request) {
    if (userRepository.read(request.getAuthorId()) == null) {
      throw DiscodeitNotFoundException.user(request.getAuthorId());
    }

    if (channelRepository.read(request.getChannelId()) == null) {
      throw DiscodeitNotFoundException.channel(request.getChannelId());
    }

    Message message = new Message(
        request.getContent(),
        request.getChannelId(),
        request.getAuthorId(),
        null
    );

    messageRepository.create(message);
    return message;
  }

  @Override
  public Message read(UUID id) {
    Message message = messageRepository.read(id);
    if (message == null) {
      throw DiscodeitNotFoundException.message(id);
    }
    return message;
  }

  @Override
  public List<Message> readAllByChannelId(UUID channelId) {
    return messageRepository.readAllByChannelId(channelId);
  }

  @Override
  public void update(MessageUpdateRequest request) {
    Message message = messageRepository.read(request.getMessageId());
    if (message == null) {
      throw DiscodeitNotFoundException.message(request.getMessageId());
    }
    message.updateContent(request.getNewContent());
    messageRepository.create(message);
  }

  @Override
  public void delete(UUID id) {
    Message message = messageRepository.read(id);
    if (message == null) {
      throw DiscodeitNotFoundException.message(id);
    }
    binaryContentRepository.deleteByMessageId(id);
    messageRepository.delete(id);
  }
}