package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    public BasicMessageService(MessageRepository messageRepository, UserRepository userRepository, ChannelRepository channelRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.channelRepository = channelRepository;
    }

    @Override
    public Message createMessage(UUID channelId, UUID authorId, String message) {
        if(message == null || message.isBlank()) {
            throw new IllegalArgumentException("메세지 내용은 비어있을 수 없습니다.");
        }
        String authorName = userRepository.findById(authorId)
                .map(User::getUsername)
                .orElse("알 수 없는 사용자입니다.");
        String channelName = channelRepository.findById(channelId)
                .map(Channel::getName)
                .orElse("알 수 없는 채널입니다.");
        Message mes = new Message(channelId, authorId, message);
        messageRepository.save(mes);
        System.out.println("메시지 전송 완료 : [작성자 : " + authorName + ", 채널 : " + channelName + "] " + message);
        return mes;
    }

    @Override
    public Message readMessage(UUID id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 메세지가 존재하지 않습니다."));
    }

    @Override
    public List<Message> readMessagesByChannel(UUID channelId) {
        return messageRepository.findAll().stream()
                .filter(m -> m.getChannelId().equals(channelId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> readAllMessage() {
        return messageRepository.findAll();
    }

    @Override
    public void deleteMessage(UUID id) {
        Message mes = readMessage(id);
        messageRepository.delete(mes);
    }

    @Override
    public void updateMessage(UUID id, String message) {
        if(message == null || message.isBlank()) {
            throw new IllegalArgumentException("메세지 내용은 비어있을 수 없습니다.");
        }

        Message mes = readMessage(id);
        if(mes.getMessage().equals(message)) {
            System.out.println("변경사항이 없습니다.");
            return;
        }

        mes.update(message);
        messageRepository.save(mes);
    }
}
