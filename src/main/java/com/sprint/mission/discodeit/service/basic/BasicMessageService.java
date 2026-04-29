package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepo;
    private final ChannelRepository channelRepo;
    private final UserRepository userRepo;
    private final MessageMapper messageMapper;
    private final PageResponseMapper pageResponseMapper;
    private final BinaryContentService binaryContentService;

    @Override
    @Transactional
    public MessageDto create(MessageCreateRequest dto, List<MultipartFile> attachments) {
        Channel channel = channelRepo.findById(dto.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        User author = userRepo.findById(dto.authorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<BinaryContent> binaryContents = binaryContentService.createAll(attachments);

        Message message = new Message(dto.content(), channel, author, binaryContents);
        messageRepo.save(message);
        log.info("Message created. messageId={}, channelId={}, authorId={}",
                message.getId(), channel.getId(), author.getId());

        return messageMapper.toDto(message);
    }

    @Override
    public MessageDto findById(UUID id) {
        Message message = messageRepo.findWithDetailsById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));
        return messageMapper.toDto(message);
    }

    @Override
    public PageResponse<MessageDto> findAllByChannelId(UUID id, Instant cursor) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        List<Message> messages;
        if(cursor == null) {
            messages = messageRepo.findTop51ByChannelOrderByCreatedAtDesc(channel);
        } else {
            messages = messageRepo.findTop51ByChannelAndCreatedAtLessThanOrderByCreatedAtDesc(channel, cursor);
        }

        return pageResponseMapper.toCursorDto(
                messages,
                50,
                messageMapper::toDto,
                Message::getCreatedAt
        );
    }

    @Override
    @Transactional
    public void update(UUID id, MessageUpdateRequest dto) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        message.update(dto.newContent());
        log.info("Message updated. messageId={}", message.getId());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Message message = messageRepo.findWithDetailsById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        binaryContentService.deleteAll(message.getAttachments());

        messageRepo.delete(message);
        log.info("Message deleted. messageId={}", message.getId());
    }
}
