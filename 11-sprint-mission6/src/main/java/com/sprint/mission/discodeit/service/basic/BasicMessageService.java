package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private static final int MESSAGE_PAGE_SIZE = 50;

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;
  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;

  @Override
  @Transactional
  public MessageDto create(
          MessageCreateRequest messageCreateRequest,
          List<BinaryContentCreateRequest> binaryContentCreateRequests
  ) {
    UUID channelId = messageCreateRequest.channelId();
    UUID authorId = messageCreateRequest.authorId();

    Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() ->
                    new NoSuchElementException("Channel with id " + channelId + " does not exist"));

    User author = userRepository.findById(authorId)
            .orElseThrow(() ->
                    new NoSuchElementException("Author with id " + authorId + " does not exist"));

    List<BinaryContent> attachments = binaryContentCreateRequests.stream()
            .map(attachmentRequest -> {
              byte[] bytes = attachmentRequest.bytes();

              BinaryContent binaryContent = new BinaryContent(
                      attachmentRequest.fileName(),
                      (long) bytes.length,
                      attachmentRequest.contentType()
              );

              BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);
              binaryContentStorage.put(savedBinaryContent.getId(), bytes);

              return savedBinaryContent;
            })
            .toList();

    Message message = new Message(messageCreateRequest.content(), channel, author, attachments);
    Message savedMessage = messageRepository.save(message);

    return messageMapper.toDto(savedMessage);
  }

  @Override
  public MessageDto find(UUID messageId) {
    return messageRepository.findById(messageId)
            .map(messageMapper::toDto)
            .orElseThrow(() ->
                    new NoSuchElementException("Message with id " + messageId + " not found"));
  }

  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, int page) {
    Pageable pageable = PageRequest.of(
            page,
            MESSAGE_PAGE_SIZE,
            Sort.by(Sort.Direction.DESC, "createdAt")
    );

    Slice<Message> messageSlice = messageRepository.findAllByChannelId(channelId, pageable);
    Slice<MessageDto> dtoSlice = messageSlice.map(messageMapper::toDto);

    return pageResponseMapper.toDto(dtoSlice);
  }

  @Override
  @Transactional
  public MessageDto update(UUID messageId, MessageUpdateRequest request) {
    Message message = messageRepository.findById(messageId)
            .orElseThrow(() ->
                    new NoSuchElementException("Message with id " + messageId + " not found"));

    message.update(request.newContent());
    return messageMapper.toDto(message);
  }

  @Override
  @Transactional
  public void delete(UUID messageId) {
    if (!messageRepository.existsById(messageId)) {
      throw new NoSuchElementException("Message with id " + messageId + " not found");
    }
    messageRepository.deleteById(messageId);
  }
}