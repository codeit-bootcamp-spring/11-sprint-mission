package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.messagedto.CreateMessageDto;
import com.sprint.mission.discodeit.dto.messagedto.MessageInfoDto;
import com.sprint.mission.discodeit.dto.messagedto.UpdateMessageDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {


  private final MessageRepository messageRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;


  @Override
  public MessageInfoDto create(CreateMessageDto createMessageDto, List<MultipartFile> attachments) {

    Message message = new Message(

        createMessageDto.authorId(),
        createMessageDto.channelId(),
        createMessageDto.content(),
        null
    );

    if (!userRepository.isExistUser(createMessageDto.authorId())) {
      throw new NonExistException("존재하는 유저 아이디가 아닙니다.");
    }
    if (!channelRepository.isExistChannel(createMessageDto.channelId())) {
      throw new NonExistException("존재하는 채널이 아닙니다.");
    }

    if (attachments != null) {
      attachments.forEach(binaryFile -> {
        try {
          binaryContentRepository.saveBinaryContent(new BinaryContent(
              createMessageDto.authorId(),
              message.getId(),
              binaryFile.getOriginalFilename(),
              binaryFile.getContentType(),
              binaryFile.getBytes(),
              binaryFile.getSize()
          ));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    }

    //binaryContent id 리스트 뽑아서
    List<UUID> attachmentIds = binaryContentRepository.getAllByMessageId(message.getId()).stream()
        .map(BinaryContent::getId)
        .toList();
    //콘텐츠 리스트 수정
    message.updateAttachmentIds(attachmentIds);
    messageRepository.saveMessage(message);

    return messageToInfo(message);
  }

  @Override
  public MessageInfoDto find(UUID messageId) {

    Message message = messageRepository.getMessage(messageId).orElseThrow();
    return messageToInfo(message);

  }

  @Override
  public List<MessageInfoDto> findAllById(UUID channelId) {
    return messageRepository.getAllByChannelId(channelId)
        .stream()
        .map(this::messageToInfo)
        .toList();
  }


  @Override
  public boolean updateMessage(UUID messageId, UpdateMessageDto updateMessageDto) {

    Message message = messageRepository.getMessage(messageId).orElseThrow();

    message.updateMessage(updateMessageDto.newContent());

    messageRepository.saveMessage(message);

    return true;
  }

  @Override
  public boolean deleteMessage(UUID messageId) {

    if (!messageRepository.isExistMessage(messageId)) {
      throw new NonExistException("해당 메시지가 존재하지 않습니다.");
    }

    messageRepository.deleteMessage(messageId);
    binaryContentRepository.deleteBinaryContentByMessageId(messageId);

    return true;
  }


  MessageInfoDto messageToInfo(Message message) {

    return new MessageInfoDto(

        message.getId(),
        message.getCreatedAt(),
        message.getUpdatedAt(),
        message.getMessage(),
        message.getChannelId(),
        message.getSenderId(),
        message.getAttachmentIds()
    );
  }


}