package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;
    private final MessageMapper messageMapper;
    private final BinaryContentStorage binaryContentStorage;
    private final PageResponseMapper pageResponseMapper;

    @Override
    @Transactional
    public MessageDto create(MessageCreateRequest request) {
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));

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

        Message message = new Message(
                author,
                channel,
                request.content()
        );

        message.updateAttachments(attachments);

        Message savedMessage = messageRepository.save(message);
        return messageMapper.toDto(savedMessage);
    }

    @Override
    public Optional<MessageDto> find(UUID id) {
        return messageRepository.findById(id)
                .map(messageMapper::toDto);
    }

    @Override
    public PageResponse<MessageDto> findAllByChannelId(UUID channelId, int page) {
        Pageable pageable = PageRequest.of(
                page,
                50,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Slice<MessageDto> slice = messageRepository.findAllByChannel_Id(channelId, pageable)
                .map(messageMapper::toDto);

        return pageResponseMapper.fromSlice(slice);
    }

    @Override
    @Transactional
    public MessageDto update(MessageUpdateParam param) {
        Message message = messageRepository.findById(param.id())
                .orElseThrow(() -> new IllegalArgumentException("해당 메시지가 존재하지 않습니다."));

        message.update(param.request().newContent());
        return messageMapper.toDto(message);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 메시지가 존재하지 않습니다."));

        for (BinaryContent attachment : message.getAttachments()) {
            binaryContentRepository.deleteById(attachment.getId());
        }

        messageRepository.deleteById(id);
    }
}