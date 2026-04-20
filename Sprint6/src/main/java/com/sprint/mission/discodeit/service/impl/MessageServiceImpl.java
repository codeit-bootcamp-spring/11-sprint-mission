package com.sprint.mission.discodeit.service.impl;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto; // DTO 임포트 확인
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent; // 엔티티 임포트 추가
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper; // 매퍼 임포트 확인
import com.sprint.mission.discodeit.repository.BinaryContentRepository; // 추가
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService; // 추가
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // 추가
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentService binaryContentService; // 주입 추가
  private final BinaryContentRepository binaryContentRepository; // 주입 추가

  @Override
  @Transactional
  public MessageDto create(MessageCreateRequest request,
      List<BinaryContentCreateRequest> binaryRequests) {
    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> new NoSuchElementException("작성자를 찾을 수 없습니다."));
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new NoSuchElementException("채널을 찾을 수 없습니다."));

    List<BinaryContent> savedAttachments = new ArrayList<>();
    if (binaryRequests != null && !binaryRequests.isEmpty()) {
      for (BinaryContentCreateRequest br : binaryRequests) {
        BinaryContentDto savedDto = binaryContentService.create(br);
        // getReferenceById를 통해 프록시 객체로 리스트 구성
        savedAttachments.add(binaryContentRepository.getReferenceById(savedDto.id()));
      }
    }

    Message message = Message.builder()
        .content(request.content())
        .author(author)
        .channel(channel)
        .attachments(savedAttachments)
        .build();

    Message savedMessage = messageRepository.save(message);
    return MessageMapper.toDto(savedMessage);
  }

  // ... 나머지 find, update, delete 로직 유지
}