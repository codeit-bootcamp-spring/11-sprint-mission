package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.MessageCreateRequestDto;
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

    public List<Message> findAll() {
        return messageRepo.findAll();
    }

    public void update(UUID id, Message newMessage) {
        Message oldMessage = findById(id);

        oldMessage.setContents(newMessage.getContents());
        oldMessage.setUserId(newMessage.getUserId());
        oldMessage.setChannelId(newMessage.getChannelId());
        oldMessage.setAttachmentIds(newMessage.getAttachmentIds());
        oldMessage.update();

        messageRepo.save(oldMessage);
    }

    public void delete(Message message) {
        messageRepo.delete(message);
    }
}
