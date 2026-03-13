package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.MessageCreateRequestDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequestDto;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepo;
    private final ChannelRepository channelRepo;
    private final UserRepository userRepo;
    private final BinaryContentRepository binaryContentRepo;

    public Message create(MessageCreateRequestDto dto) {
        channelRepo.findById(dto.channelId())
                .orElseThrow(() -> new ChannelNotFoundException(dto.channelId()));

        userRepo.findById(dto.userId())
                .orElseThrow(() -> new UserNotFoundException(dto.userId()));

        Message message = new Message(dto.contents(), dto.userId(), dto.channelId(), dto.attachmentIds());
        messageRepo.save(message);
        return message;
    }

    public Message findById(UUID id) {
        return messageRepo.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));
    }

    public List<Message> findAllByChannelId(UUID id) {
        channelRepo.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));

        return messageRepo.findAll().stream()
                .filter(p -> p.getChannelId().equals(id))
                .toList();
    }

    public void update(MessageUpdateRequestDto dto) {
        Message message = messageRepo.findById(dto.messageId())
                .orElseThrow(() -> new MessageNotFoundException(dto.messageId()));

        message.setContents(dto.contents());
        message.setAttachmentIds(dto.attachmentIds());
        message.update();

        messageRepo.save(message);
    }

    public void delete(UUID id) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));

        messageRepo.delete(message);
    }
}
