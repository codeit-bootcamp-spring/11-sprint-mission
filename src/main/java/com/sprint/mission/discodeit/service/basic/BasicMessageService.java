package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final MessageMapper messageMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final PageResponseMapper pageResponseMapper;

  @Override
  @Transactional
  public MessageDto create(MessageCreateRequest dto,
      List<BinaryContentCreateRequest> binaryContentDto) {
    User author = userRepository.findById(dto.authorId())
        .orElseThrow(
            () -> new NoSuchElementException(
                "Author with id " + dto.authorId() + " does not exist"));
    Channel channel = channelRepository.findById(dto.channelId())
        .orElseThrow(() -> new NoSuchElementException(
            "Channel with id " + dto.channelId() + " does not exist"));

    List<BinaryContent> attachments = binaryContentDto.stream()
        .map(request -> new BinaryContent(request.fileName(),
            (long) request.bytes().length, request.contentType()))
        .toList();

    attachments = binaryContentRepository.saveAll(attachments);

    for (int i = 0; i < attachments.size(); i++) {
      binaryContentStorage.put(attachments.get(i).getId(), binaryContentDto.get(i).bytes());
    }

    Message message = new Message(author, channel, dto.content(), attachments);
    messageRepository.save(message);

    UserStatus status = author.getStatus();
    status.updateTime(Instant.now());

    return messageMapper.toDto(message);
  }

  // 채팅방에 들어가서 위로 스크롤하면 예전 메시지가 계속 뜨는 로직
  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor) {
    if (!channelRepository.existsById(channelId)) {
      throw new NoSuchElementException("Channel with id " + channelId + " does not exist");
    }

    Pageable pageable = PageRequest.of(0, 50);

    Slice<Message> slice;

    if (cursor == null) {
      // 처음엔 무조건 맨 처음부터 최신순으로 50개
      slice = messageRepository.findByChannelIdOrderByCreatedAtDesc(channelId,
          pageable);
    } else {
      // 커서(시간)보다 옛날 메시지를 최신순으로 50개
      slice = messageRepository.findByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc(channelId,
          cursor, pageable);
    }

    Slice<MessageDto> dtoSlice = slice.map(messageMapper::toDto);

    return pageResponseMapper.fromSlice(dtoSlice, MessageDto::createdAt);
  }

  @Override
  @Transactional
  public MessageDto update(UUID id, MessageUpdateRequest dto) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Message with id " + id + " not found"));

    message.update(dto.newContent());

    UserStatus status = message.getAuthor().getStatus();
    status.updateTime(Instant.now());

    return messageMapper.toDto(message);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Message with id " + id + " not found"));

    if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
      binaryContentRepository.deleteAll(message.getAttachments());
    }
    messageRepository.delete(message);
  }
}