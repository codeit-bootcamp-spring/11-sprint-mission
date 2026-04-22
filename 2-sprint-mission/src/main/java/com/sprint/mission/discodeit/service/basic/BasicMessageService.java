package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageDto.CreateRequest;
import com.sprint.mission.discodeit.dto.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;
  private final BinaryContentStorage binaryContentStorage;

  @Override
  @Transactional
  public MessageDto.Response create(CreateRequest request,
      List<BinaryContentDto.CreateRequest> fileRequests) {
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    List<BinaryContent> attachments = (fileRequests != null)
        ? fileRequests.stream().map(BinaryContentDto.CreateRequest::toEntity).toList()
        : new ArrayList<>();

    if (!attachments.isEmpty()) {
      binaryContentRepository.saveAll(attachments);

      for (int i = 0; i < attachments.size(); i++) {
        BinaryContent entity = attachments.get(i);
        BinaryContentDto.CreateRequest fileReq = fileRequests.get(i);
        binaryContentStorage.put(entity.getId(), fileReq.bytes());
      }
    }

    Message message = request.toEntity(channel, author, attachments);
    messageRepository.save(message);

    return messageMapper.toDto(message);
  }


  @Override
  public PageResponse<MessageDto.Response> findAllByChannelId(UUID channelId, Instant cursor,
      Pageable pageable) {
    Instant effectiveCursor =
        (cursor != null) ? cursor : Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS);
    Slice<Message> messageSlice = messageRepository.findAllByChannelId(channelId, effectiveCursor,
        pageable);
    Slice<MessageDto.Response> responseSlice = messageSlice.map(messageMapper::toDto);

    Instant nextCursor = null;
    if (messageSlice.hasNext() && !messageSlice.getContent().isEmpty()) {
      List<Message> content = messageSlice.getContent();
      nextCursor = content.get(content.size() - 1).getCreatedAt();
    }
    return pageResponseMapper.fromSlice(responseSlice, nextCursor);
  }

  @Override
  @Transactional
  public MessageDto.Response update(UUID id, MessageDto.UpdateRequest request) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));
    message.update(request.newContent());

    return messageMapper.toDto(message);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!messageRepository.existsById(id)) {
      throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
    }

    messageRepository.deleteById(id);
  }
}