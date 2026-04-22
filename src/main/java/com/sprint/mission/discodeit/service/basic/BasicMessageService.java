package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.messagedto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.messagedto.MessageDto;
import com.sprint.mission.discodeit.dto.messagedto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {


  private final JPAMessageRepository messageRepository;
  private final JPAChannelRepository channelRepository;
  private final JPAUserRepository userRepository;

  private final MessageMapper messageMapper;
  private final PageResponseMapper<MessageDto> pageResponseMapper;

  private final BinaryContentStorage binaryContentStorage;


  @Override
  @Transactional
  public MessageDto create(MessageCreateRequest messageCreateRequest,
      List<MultipartFile> attachments) {

    //존재 유저, 채널 체크
    User user = userRepository.findById(messageCreateRequest.authorId())
        .orElseThrow(() -> new NonExistException("존재하지 않는 유저 아이디 입니다."));
    Channel channel = channelRepository.findById(messageCreateRequest.channelId())
        .orElseThrow(() -> new NonExistException("존재하지 않는 채널 아이디 입니다."));

    //메시지 생성
    Message message = new Message(

        user,
        channel,
        messageCreateRequest.content(),
        new ArrayList<>()
    );
    List<BinaryContent> binaryContents = new ArrayList<>();

    // 파일들이 들어왔을 때
    if (attachments != null) {
      attachments.forEach(binaryFile -> {
        try {

          //메타데이터 만들기
          BinaryContent binaryContent = new BinaryContent(
              binaryFile.getOriginalFilename(),
              binaryFile.getContentType(),
              binaryFile.getSize()
          );
          //데이터 저장하기.
          binaryContentStorage.put(binaryContent.getId(), binaryFile.getBytes());

          //파일리스트에 등록
          binaryContents.add(binaryContent);


        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      //첨부파일 목록 업데이트
      message.updateAttachments(binaryContents);
    }

    //메시지 저장 + 영속성 전이로 메타데이터도 저장
    messageRepository.save(message);

    return messageMapper.toDto(message);

  }

  @Override
  @Transactional(readOnly = true)
  public MessageDto find(UUID messageId) {

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new NonExistException("존재하지 않는 메시지 아이디 입니다."));
    return messageMapper.toDto(message);

  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Pageable pageable,
      Instant cursor) {

    //채널 존재 여부 체크
    if (!channelRepository.existsById(channelId)) {
      throw new NonExistException("존재하지 않는 채널 아이디입니다.");
    }

    Slice<Message> list;
    if (cursor != null) {
      list = messageRepository.findAllByChannel_Id(channelId, pageable, cursor);
    } else {
      list = messageRepository.findAllByChannel_Id(channelId, pageable);
    }

    Instant nextCursor;

    if (list.hasNext()) {
      nextCursor = list.getContent().get(list.getContent().size() - 1).getCreatedAt();
    } else {
      nextCursor = null;
    }

    Slice<MessageDto> listDto = list.map(messageMapper::toDto);

    return pageResponseMapper.fromSlice(listDto, nextCursor);

  }


  @Override
  @Transactional
  public MessageDto update(UUID messageId, MessageUpdateRequest messageUpdateRequest) {

    //메시지 가져오기
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new NonExistException("존재하지 않는 메시지 아이디 입니다."));

    //업데이트 -> dirty check
    message.updateContent(messageUpdateRequest.newContent());

    return messageMapper.toDto(message);


  }

  @Override
  @Transactional
  public void delete(UUID messageId) {

    if (!messageRepository.existsById(messageId)) {
      throw new NonExistException("존재하지 않는 메시지 아이디입니다.");
    }

    messageRepository.deleteById(messageId);
  }


}