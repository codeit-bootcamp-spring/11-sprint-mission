package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdate;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;

  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;

  @Override
  @Transactional
  public MessageDto createMessage(CreateMessageRequest request,
      List<MultipartFile> attachments) {
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHANNEL_NOT_FOUND));
    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

    Message message = new Message(channel, author, request.content());

    if (attachments != null && !attachments.isEmpty()) {
      for (MultipartFile file : attachments) {
        try {
          BinaryContent content = new BinaryContent(
              file.getOriginalFilename(),
              file.getContentType(),
              file.getSize()
          );
          BinaryContent savedContent = binaryContentRepository.save(content);
          binaryContentStorage.put(savedContent.getId(), file.getBytes());
          message.addAttachment(savedContent);
        } catch (IOException e) {
          throw new BusinessLogicException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }
      }
    }

    Message savedMessage = messageRepository.save(message);

    return messageMapper.toDto(savedMessage);
  }

  @Override
  public PageResponse<MessageDto> readMessagesByChannel(UUID channelId, int page) {
    Pageable pageable = PageRequest.of(page, 50, Sort.by("createdAt").descending());

    Slice<Message> messageSlice = messageRepository.findByChannelId(channelId, pageable);

    Slice<MessageDto> dtoSlice = messageSlice.map(messageMapper::toDto);

    return pageResponseMapper.fromSlice(dtoSlice);
  }

  @Override
  @Transactional
  public void deleteMessage(UUID id) {
    Message mes = messageRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MESSAGE_NOT_FOUND));

    if (!mes.getAttachments().isEmpty()) {
      binaryContentRepository.deleteAll(mes.getAttachments());
    }

    messageRepository.delete(mes);
  }

  @Override
  @Transactional
  public MessageDto updateMessage(UUID id, MessageUpdate request) {
    Message mes = messageRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MESSAGE_NOT_FOUND));

    mes.update(request.newContent());

    return messageMapper.toDto(mes);
  }
}
