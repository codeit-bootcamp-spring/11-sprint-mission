package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.InvalidException;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final MessageMapper messageMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    @Transactional
    public MessageDto create(MessageCreateRequest request) {
        log.info("메세지 생성 시작: authorId={}, channelId={}",
                request.authorId(),
                request.channelId()
        );

        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> {
                    log.warn("메세지 생성 실페 - 작성자 없음: authorId={}", request.authorId());
                    return new UserNotFoundException(request.authorId());
                });

        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> {
                    log.warn("메시지 생성 실페 - 채널 없음: channelId={}", request.channelId());
                    return new ChannelNotFoundException(request.channelId());
                });

        List<BinaryContent> attachments = new ArrayList<>();

        if (request.attachments() != null) {
            for (BinaryContentCreateRequest attachmentRequest : request.attachments()) {
                BinaryContent attachment = new BinaryContent(
                        attachmentRequest.fileName(),
                        attachmentRequest.bytes() == null ? 0 : attachmentRequest.bytes().length,
                        attachmentRequest.contentType()
                );

                BinaryContent savedAttachment = binaryContentRepository.save(attachment);

                if (attachmentRequest.bytes() != null) {
                    binaryContentStorage.put(savedAttachment.getId(), attachmentRequest.bytes());
                }

                attachments.add(savedAttachment);
            }
        }

        Message message = new Message(author, channel, request.content());
        message.updateAttachments(attachments);

        Message savedMessage = messageRepository.save(message);

        log.info("메세지 생성 완료: messageId={}, attachmentsCount={}", savedMessage.getId(), attachments.size());
        return messageMapper.toDto(savedMessage);
    }

    @Override
    public Optional<MessageDto> find(UUID id) {
        return messageRepository.findByIdWithDetails(id)
                .map(messageMapper::toDto);
    }

    @Override
    public PageResponse<MessageDto> findAllByChannelId(UUID channelId, String cursor, int size) {
        log.debug("메시지 목록 조회 시작: channelId={}, cursor={}, requestedSize={}",
                channelId,
                cursor,
                size
        );

        int pageSize = size <= 0 ? 50 : Math.min(size, 50);

        Instant cursorCreatedAt = null;
        UUID cursorId = null;

        if (cursor != null && !cursor.isBlank()) {
            try {
                String[] parts = cursor.split("_", 2);
                cursorCreatedAt = Instant.parse(parts[0]);
                cursorId = UUID.fromString(parts[1]);
            } catch (Exception e) {
                log.warn("메시지 목록 조회 실패 - cursor 형식 오류: cursor={}", cursor);
                throw new DiscodeitException(
                        ErrorCode.INVALID_REQUEST,
                        Map.of("cursor", cursor)
                );
            }
        }

        List<UUID> ids = messageRepository.findPageIdsByChannelIdAndCursor(
                channelId,
                cursorCreatedAt,
                cursorId,
                PageRequest.of(0, pageSize + 1)
        );

        boolean hasNext = ids.size() > pageSize;
        if (hasNext) {
            ids = ids.subList(0, pageSize);
        }

        if (ids.isEmpty()) {
            return new PageResponse<>(List.of(), null, pageSize);
        }

        List<MessageDto> content = messageRepository.findAllWithDetailsByIdIn(ids).stream()
                .map(messageMapper::toDto)
                .toList();

        String nextCursor = null;
        if (hasNext) {
            MessageDto last = content.get(content.size() - 1);
            nextCursor = last.createdAt().toString() + "_" + last.id();
        }

        log.debug("메시지 목록 조회 완료: channelId={}, resultSize={}, hasNext={}",
                channelId,
                content.size(),
                hasNext
        );

        return new PageResponse<>(content, nextCursor, pageSize);
    }

    @Override
    @Transactional
    public MessageDto update(MessageUpdateParam param) {
        log.info("메시지 수정 시작: messageId={}", param.id());

        Message message = messageRepository.findById(param.id())
                .orElseThrow(() -> {
                    log.warn("메시지 수정 실패 - 메시지 없음: messageId={}", param.id());
                    return new MessageNotFoundException(param.id());
                });

        message.update(param.request().newContent());

        log.info("메시지 수정 완료: messageId={}", message.getId());

        return messageMapper.toDto(message);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("메시지 삭제 시작: messageId={}", id);

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("메시지 삭제 실패 - 메시지 없음: messageId={}", id);
                    return new MessageNotFoundException(id);
                });

        messageRepository.delete(message);

        for (BinaryContent attachment : message.getAttachments()) {
            binaryContentRepository.deleteById(attachment.getId());
            binaryContentStorage.delete(attachment.getId());

            log.debug("메시지 첨부파일 삭제 완료: messageId={}, attachmentId={}",
                    id,
                    attachment.getId()
            );
        }

        messageRepository.deleteById(id);

        log.info("메시지 삭제 완료: messageId={}",
                id
        );
    }
}