package com.sprint.mission.discodeit.service.basic;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_NOT_FOUND;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.MESSAGE_CHANNEL_ACCESS_REQUIRED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.MESSAGE_CONTENT_REQUIRED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.MESSAGE_NOT_FOUND;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_NOT_FOUND;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ApiException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageMapper mapper;
  private final BinaryContentStorage binaryContentStorage;

  @Transactional
  @Override
  public MessageResponse createMessage(MessageCreateRequest messageCreateRequest,
      List<BinaryContentCreateRequest> binaryContentCreateRequests) {
    if (messageCreateRequest.content() == null || messageCreateRequest.content().isBlank()) {
      throw new ApiException(MESSAGE_CONTENT_REQUIRED);
    }

    User author = this.userRepository.findById(messageCreateRequest.authorId())
        .orElseThrow(() -> new ApiException(USER_NOT_FOUND));
    Channel channel = this.channelRepository.findById(messageCreateRequest.channelId())
        .orElseThrow(() -> new ApiException(CHANNEL_NOT_FOUND));

    if (channel.isPrivate() && !this.readStatusRepository.existsByUserAndChannel(author, channel)) {
      throw new ApiException(MESSAGE_CHANNEL_ACCESS_REQUIRED);
    }

    List<BinaryContent> attachments = new ArrayList<>();
    if (binaryContentCreateRequests != null && !binaryContentCreateRequests.isEmpty()) {
      attachments = binaryContentCreateRequests.stream()
          .map(req -> {
            BinaryContent attachment = new BinaryContent(req.fileName(), req.size(),
                req.contentType());
            this.binaryContentStorage.put(attachment.getId(), req.bytes());
            return attachment;
          })
          .toList();
    }

    Message message = new Message(
        messageCreateRequest.content(),
        channel,
        author,
        attachments
    );
    this.messageRepository.save(message);

    log.info("Message has been created successfully. ✅ [ID: {}]", message.getId());
    log.info("-> {channel: {}, sender: {}, content: {}}",
        channel.isPrivate() ? '-' : channel.getName(), author.getUsername(), message.getContent());
    return this.mapper.toResponse(message);
  }

  @Override
  public List<MessageResponse> findAllByChannelId(UUID channelId) {
    return this.messageRepository.findAllByChannelId(channelId).stream()
        .map(this.mapper::toResponse)
        .toList();
  }

  @Transactional
  @Override
  public MessageResponse updateMessage(UUID id, MessageUpdateRequest messageUpdateRequest,
      List<BinaryContentCreateRequest> binaryContentCreateRequests) {
    Message message = this.messageRepository.findById(id)
        .orElseThrow(() -> new ApiException(MESSAGE_NOT_FOUND));

    if (messageUpdateRequest != null) {
      if (messageUpdateRequest.newContent() != null && !messageUpdateRequest.newContent()
          .isBlank()) {
        message.updateContent(messageUpdateRequest.newContent());
      }
    }

    List<BinaryContent> newAttachments = new ArrayList<>();
    if (binaryContentCreateRequests != null && !binaryContentCreateRequests.isEmpty()) {
      newAttachments = binaryContentCreateRequests.stream()
          .map(req -> {
            BinaryContent attachment = new BinaryContent(req.fileName(), req.size(),
                req.contentType());
            this.binaryContentStorage.put(attachment.getId(), req.bytes());
            return attachment;
          })
          .toList();
    }
    if (!newAttachments.isEmpty()) {
      message.replaceAttachments(newAttachments);
    }

    log.info("Message has been updated successfully. ✅ [ID: {}]", id);
    return this.mapper.toResponse(message);
  }

  @Transactional
  @Override
  public void deleteMessage(UUID id) {
    Message message = this.messageRepository.findById(id)
        .orElseThrow(() -> new ApiException(MESSAGE_NOT_FOUND));

    this.messageRepository.delete(message);

    log.info("Message has been deleted successfully. ✅ [ID: {}]", id);
  }
}
