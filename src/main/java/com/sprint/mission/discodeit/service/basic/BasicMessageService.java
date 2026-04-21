package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final MessageMapper messageMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final PageResponseMapper pageResponseMapper;

  @Override
  @Transactional
  public MessageDto create(MessageCreateRequest request, List<MultipartFile> attachments) {
    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지  않습니다."));
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));

    Message message = new Message(request.content(), author, channel);

    if (attachments != null && !attachments.isEmpty()) {
      List<BinaryContent> attachmentContents = new ArrayList<>();
      for (MultipartFile file : attachments) {
        try {
          BinaryContent content = new BinaryContent(
              file.getOriginalFilename(), file.getSize(), file.getContentType());

          attachmentContents.add(content);

          binaryContentStorage.put(content.getId(), file.getBytes());
        } catch (IOException e) {
          throw new RuntimeException("파일 처리 중 오류 발생", e);
        }
      }
      message.setAttachments(attachmentContents);
    }
    messageRepository.save(message);
    return messageMapper.toDto(message);
  }

  @Override
  public MessageDto findById(UUID id) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));
    return messageMapper.toDto(message);
  }

  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor, int size) {
    PageRequest pageRequest = PageRequest.of(0, size);
    Slice<Message> slice;

    if (cursor == null) {
      slice = messageRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageRequest);
    } else {
      slice = messageRepository.findByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc(
          channelId, cursor, pageRequest);
    }

    Slice<MessageDto> dtoSlice = slice.map(messageMapper::toDto);
    String nextCursor = null;

    if (slice.hasNext() && !slice.getContent().isEmpty()) {
      List<Message> messages = slice.getContent();
      nextCursor = messages.get(messages.size() - 1).getCreatedAt().toString();
    }

    return pageResponseMapper.fromSlice(dtoSlice, nextCursor);
  }

  @Override
  @Transactional
  public MessageDto update(UUID id, MessageUpdateRequest request) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));
    message.update(request.newContent());
    return messageMapper.toDto(message);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    messageRepository.deleteById(id);
  }
}
