package com.sprint.mission.discodeit.service.utils;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("messageGuard")
@RequiredArgsConstructor
public class MessageGuard {

    private final MessageRepository messageRepository;

    public boolean isAuthor(UUID messageId, DiscodeitUserDetails principal) {
        if (principal == null) {
            return false;
        }

        // DB에서 메시지를 조회하여 작성자 ㅑㅇ랑(authorId)와 로그인한 유저 ID를 비교
        return messageRepository.findById(messageId)
                .map(message -> message.getAuthor().getId().equals(principal.getUserDto().id()))
                .orElse(false); // 메시지가 존재하지 않으면 보호 차원에서 false 반환
    }
}
