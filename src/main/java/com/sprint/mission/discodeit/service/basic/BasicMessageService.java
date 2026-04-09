package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepo;
    private final ChannelRepository channelRepo;
    private final UserRepository userRepo;
    private final BinaryContentRepository binaryContentRepo;

    @Override
    @Transactional
    public MessageDto create(MessageCreateRequest dto, List<MultipartFile> attachments) {
        Channel channel = channelRepo.findById(dto.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        User author = userRepo.findById(dto.authorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<BinaryContent> binaryContents = saveAttachments(attachments);

        Message message = new Message(dto.content(), channel, author, binaryContents);
        messageRepo.save(message);

        return toDto(message);
    }

    @Override
    public MessageDto findById(UUID id) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));
        return toDto(message);
    }

    @Override
    public List<MessageDto> findAllByChannelId(UUID id) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        // n+1 문제 해결 필요
        return messageRepo.findAllByChannel(channel).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void update(UUID id, MessageUpdateRequest dto) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        message.update(dto.newContent());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        // n+1 문제 해결 필요
        for(BinaryContent binaryContent : message.getAttachments()) {
            binaryContentRepo.delete(binaryContent);
        }

        messageRepo.delete(message);
    }

    private MessageDto toDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getContent(),
                message.getAuthor().getId(),
                message.getChannel().getId(),
                message.getAttachments().stream()
                        .map(BinaryContent::getId)
                        .toList()
        );
    }

    private List<BinaryContent> saveAttachments(List<MultipartFile> attachments) {
        if(attachments == null || attachments.isEmpty()) {
            return List.of();
        }
        return attachments.stream()
                .filter(p -> !p.isEmpty())
                .map(this::saveAttachment)
                .toList();
    }

    private BinaryContent saveAttachment(MultipartFile file) {
        try {
            BinaryContent binaryContent = new BinaryContent(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );
            binaryContentRepo.save(binaryContent);
            return binaryContent;
        } catch (IOException e){
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }
}
