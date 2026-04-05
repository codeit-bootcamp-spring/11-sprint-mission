package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentSaveException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
    public MessageDto create(MessageCreateRequest dto, List<MultipartFile> attachments) {
        channelRepo.findById(dto.channelId())
                .orElseThrow(() -> new ChannelNotFoundException(dto.channelId()));

        userRepo.findById(dto.authorId())
                .orElseThrow(() -> new UserNotFoundException(dto.authorId()));

        List<UUID> attachmentIds = saveAttachments(attachments);

        Message message = new Message(dto.content(), dto.authorId(), dto.channelId(), attachmentIds);
        messageRepo.save(message);
        return toDto(message);
    }

    @Override
    public MessageDto findById(UUID id) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));
        return toDto(message);
    }

    @Override
    public List<MessageDto> findAllByChannelId(UUID id) {
        channelRepo.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));

        return messageRepo.findAll().stream()
                .filter(p -> p.getChannelId().equals(id))
                .map(this::toDto)
                .toList();
    }

    @Override
    public void update(UUID id, MessageUpdateRequest dto) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));

        message.setContents(dto.newContent());
        message.update();

        messageRepo.save(message);
    }

    @Override
    public void delete(UUID id) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));

        for(UUID binaryContentId : message.getAttachmentIds()) {
            if(!binaryContentRepo.deleteById(binaryContentId)) {
                throw new BinaryContentNotFoundException(binaryContentId);
            }
        }
        if(!messageRepo.deleteById(message.getId())) {
            throw new MessageNotFoundException(message.getId());
        }
    }

    private MessageDto toDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getContents(),
                message.getUserId(),
                message.getChannelId(),
                message.getAttachmentIds()
        );
    }

    private List<UUID> saveAttachments(List<MultipartFile> attachments) {
        if(attachments == null || attachments.isEmpty()) {
            return List.of();
        }
        return attachments.stream()
                .filter(p -> !p.isEmpty())
                .map(this::saveAttachment)
                .toList();
    }

    private UUID saveAttachment(MultipartFile file) {
        if(file == null || file.isEmpty()) {
            return null;
        }

        try {
            BinaryContent binaryContent = new BinaryContent(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );
            binaryContentRepo.save(binaryContent);
            return binaryContent.getId();
        } catch (IOException e){
            throw new BinaryContentSaveException(file.getOriginalFilename(), e);
        }
    }
}
