package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicMessageService implements MessageService {

  private static final int MESSAGE_PAGE_SIZE = 50;

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;


  @Override
  @Transactional
  public MessageDto create(MessageCreateRequest request) {
    User author = userRepository.findById(request.getAuthorId())
        .orElseThrow(() -> DiscodeitNotFoundException.user(request.getAuthorId()));

    Channel channel = channelRepository.findById(request.getChannelId())
        .orElseThrow(() -> DiscodeitNotFoundException.channel(request.getChannelId()));

    Message message = new Message(request.getContent(), channel, author);
    Message savedMessage = messageRepository.save(message);

    return messageMapper.toDto(savedMessage);
  }

  @Override
  @Transactional(readOnly = true)
  public MessageDto find(UUID id) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.message(id));

    return messageMapper.toDto(message);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, int page) {
    channelRepository.findById(channelId)
        .orElseThrow(() -> DiscodeitNotFoundException.channel(channelId));

    Pageable pageable = PageRequest.of(
        page,
        MESSAGE_PAGE_SIZE,
        Sort.by(Sort.Direction.DESC, "createdAt")
    );

    Slice<Message> messageSlice =
        messageRepository.findAllByChannel_IdOrderByCreatedAtDesc(channelId, pageable);

    return pageResponseMapper.fromSlice(messageSlice.map(messageMapper::toDto));
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
