package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.service.dto.message.MessageAttachmentRequest;
import com.sprint.mission.discodeit.service.dto.message.MessageDto;
import com.sprint.mission.discodeit.service.dto.message.UpdateMessageRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageMapper messageMapper;
    private final PageResponseMapper pageResponseMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Transactional
    public MessageDto create(CreateMessageRequest request) {
        validateCreateRequest(request);
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new UserNotFoundException(request.authorId()));
        Channel channel = getChannel(request.channelId());

        // 첨부파일 엔티티 생성 - Message cascade를 통해 함께 저장됨
        List<BinaryContent> attachments = buildAttachments(request.attachments());
        Message message = new Message(author, channel, request.content(), attachments);

        Message saved = messageRepository.save(message);
        log.info("메시지 생성 완료: id={}, channelId={}", saved.getId(), request.channelId());
        return toDto(saved);
    }

    @Transactional
    public MessageDto create(CreateMessageRequest request, List<MultipartFile> attachments) {
        CreateMessageRequest mergedRequest = new CreateMessageRequest(
                request.authorId(),
                request.channelId(),
                request.content(),
                toAttachmentRequests(attachments)
        );
        return create(mergedRequest);
    }

    public MessageDto find(UUID id) {
        return toDto(getMessage(id));
    }

    public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor, int size) {
        if (channelId == null) {
            throw new DiscodeitException(ErrorCode.CHANNEL_ID_REQUIRED);
        }
        getChannel(channelId);

        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<MessageDto> slice = (cursor == null
                ? messageRepository.findAllByChannelIdOrderByCreatedAtDesc(channelId, pageRequest)
                : messageRepository.findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(channelId, cursor, pageRequest)
        ).map(this::toDto);

        return pageResponseMapper.fromSlice(slice, dto -> dto.createdAt());
    }

    @Transactional
    public MessageDto update(UpdateMessageRequest request) {
        validateUpdateRequest(request);
        Message message = getMessage(request.messageId());
        // 변경 감지(dirty checking)
        message.update(request.content());
        log.info("메시지 수정 완료: id={}", message.getId());
        return toDto(message);
    }

    @Transactional
    public void delete(UUID id) {
        Message message = getMessage(id);
        // Message의 attachments는 orphanRemoval 설정에 의해 함께 삭제됨
        messageRepository.delete(message);
        log.info("메시지 삭제 완료: id={}", id);
    }

    private Message getMessage(UUID id) {
        if (id == null) {
            throw new DiscodeitException(ErrorCode.MESSAGE_ID_REQUIRED);
        }
        return messageRepository.findById(id)
                .orElseThrow(() -> new DiscodeitException(ErrorCode.MESSAGE_NOT_FOUND, Map.of("messageId", id)));
    }

    private Channel getChannel(UUID id) {
        if (id == null) {
            throw new DiscodeitException(ErrorCode.CHANNEL_ID_REQUIRED);
        }
        return channelRepository.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));
    }

    private List<BinaryContent> buildAttachments(List<MessageAttachmentRequest> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }
        return attachments.stream()
                .map(this::buildAttachment)
                .toList();
    }

    private BinaryContent buildAttachment(MessageAttachmentRequest attachmentRequest) {
        if (attachmentRequest == null) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "첨부파일 정보가 비어있어요.");
        }
        BinaryContent binaryContent = new BinaryContent(
                attachmentRequest.data(),
                attachmentRequest.fileName(),
                attachmentRequest.contentType()
        );
        // Message cascade 저장 전에 bytes를 스토리지에 저장 (ID는 생성자에서 이미 할당됨)
        binaryContentStorage.put(binaryContent.getId(), attachmentRequest.data());
        return binaryContent;
    }

    private List<MessageAttachmentRequest> toAttachmentRequests(List<MultipartFile> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }
        List<MessageAttachmentRequest> requests = new ArrayList<>();
        for (MultipartFile attachment : attachments) {
            if (attachment == null || attachment.isEmpty()) {
                continue;
            }
            try {
                requests.add(new MessageAttachmentRequest(
                        attachment.getBytes(),
                        attachment.getOriginalFilename(),
                        attachment.getContentType()
                ));
            } catch (IOException exception) {
                throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "첨부파일을 읽을 수 없어요.");
            }
        }
        return requests;
    }

    private MessageDto toDto(Message message) {
        return messageMapper.toDto(message);
    }

    private void validateCreateRequest(CreateMessageRequest request) {
        if (request == null) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "메시지 생성 요청값이 비어있어요.");
        }
        if (request.authorId() == null) {
            throw new DiscodeitException(ErrorCode.USER_ID_REQUIRED);
        }
        if (request.channelId() == null) {
            throw new DiscodeitException(ErrorCode.CHANNEL_ID_REQUIRED);
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new DiscodeitException(ErrorCode.MESSAGE_CONTENT_REQUIRED);
        }
    }

    private void validateUpdateRequest(UpdateMessageRequest request) {
        if (request == null) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "메시지 수정 요청값이 비어있어요.");
        }
        if (request.messageId() == null) {
            throw new DiscodeitException(ErrorCode.MESSAGE_ID_REQUIRED);
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new DiscodeitException(ErrorCode.MESSAGE_CONTENT_REQUIRED);
        }
    }
}
