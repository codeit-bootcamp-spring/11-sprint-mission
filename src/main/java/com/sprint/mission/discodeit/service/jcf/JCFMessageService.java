/*
package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ChannelService channelService;

    // MessageService에 의존성 추가
    public JCFMessageService(MessageRepository messageRepository, UserService userService, ChannelService channelService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.channelService = channelService;
    }

    // 메세지를 만들때, 검증
    @Override
    public Message create(Message message) {
        if (userService.findById(message.getAuthorId()) == null) {
            throw new IllegalStateException("존재하지 않는 유저입니다.");
        }

        if (channelService.findById(message.getChannelId()) == null) {
            throw new IllegalStateException("존재하지 않는 채널입니다.");
        }
        return messageRepository.save(message);
    }

    @Override
    public Message findById(UUID id) {
        return messageRepository.findById(id);
    }

    @Override
    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    @Override
    public Message update(Message message) {
        if (messageRepository.findById(message.getId()) == null){
            throw new IllegalStateException("존재하지 않는 채널입니다.");
        }

        if (userService.findById(message.getAuthorId()) == null){
            throw new IllegalStateException("존재하지 않는 유저입니다.");
        }

        if (channelService.findById(message.getChannelId()) == null){
            throw new IllegalStateException("존재하지 않는 채널입니다.");
        }
        return messageRepository.save(message);
    }

    @Override
    public void delete(UUID id) {
        messageRepository.delete(id);
    }
}
*/