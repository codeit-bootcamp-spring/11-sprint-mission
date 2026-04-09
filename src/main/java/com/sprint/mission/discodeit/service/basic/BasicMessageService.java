package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final MessageMapper messageMapper;

  @Override
  public MessageDto create(MessageCreateRequest messageCreateRequest,
                        List<BinaryContentCreateRequest> binaryContentCreateRequests) {
    UUID channelId = messageCreateRequest.channelId();
    UUID authorId = messageCreateRequest.authorId();

    Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new NoSuchElementException("Channel with id " + channelId + " does not exist"));
    User author = userRepository.findById(authorId)
            .orElseThrow(() -> new NoSuchElementException("Author with id " + authorId + " does not exist"));

    List<BinaryContent> attachments = binaryContentCreateRequests.stream()
            .map(req -> binaryContentRepository.save(
                    new BinaryContent(req.fileName(), (long) req.bytes().length, req.contentType())
            ))
            .toList();

    Message message = new Message(messageCreateRequest.content(), channel, author, attachments);
    Message createdMessage = messageRepository.save(message);
    return messageMapper.toDto(createdMessage);
  }

  @Override
  public MessageDto find(UUID messageId) {
    Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new NoSuchElementException("Message with id " + messageId + " not found"));
    return messageMapper.toDto(message);
  }

  @Override
  public List<MessageDto> findAllByChannelId(UUID channelId) {
    return messageRepository.findAllByChannelId(channelId).stream()
            .map(messageMapper::toDto)
            .toList();
  }

  @Override
  public MessageDto update(UUID messageId, MessageUpdateRequest request) {
    Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new NoSuchElementException("Message with id " + messageId + " not found"));
    message.update(request.newContent());
    Message updatedMessage = messageRepository.save(message);
    return messageMapper.toDto(updatedMessage);
  }

  @Override
  public void delete(UUID messageId) {
    Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new NoSuchElementException("Message with id " + messageId + " not found"));

    message.getAttachments()
            .forEach(attachment -> binaryContentRepository.deleteById(attachment.getId()));

    messageRepository.deleteById(messageId);
  }
}
