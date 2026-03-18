package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.message.*;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.UserChannel;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserChannelRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserChannelRepository userChannelRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final ChannelRepository channelRepository;

    // create
    @Override
    public MessageResponseDTO sendMessage(
            SendMessageRequestDTO dto
    ) {
        // 검증 로직
        // - 이 유저가 이 채널에 가입되어있는가?
        userJoinThisChannel(dto.userId(), dto.channelId());

        // 검증 로직 추가
        // - 해당 파일이 정말 실제 서버에 존재하는지 확인해야함
        // - 그리고 현재 profileId만 받도록 설계되어있는데 이유는 BinaryContentService에서 Id를 만들고 해당 id를 반환하도록 설계할 예정
        if (dto.attachmentIds() != null && !dto.attachmentIds().isEmpty()) {
            for (UUID fileId : dto.attachmentIds()) {
                binaryContentRepository.findById(fileId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_EXIST)); // 고민 중
            }
        }

        // 여기서 지금 사실 dto -> message를 변환하기 위해서 null 체크를 내부에서 하고 있는데
        // 로직적으로 emptyList를 만들어줘서 반환하는게 더 좋은 선택일까?
        // - dto는 null로 받는 것이 더 좋은 것 같은데
        // - dto로 생성이 아니라 내부 메소드 자체로 생성하도록 (Message.create())를 사용하는 패턴이 더 나으려나? 질문

        // 실행 로직
        Message newMessage = Message.create(dto.content(), dto.channelId(), dto.userId(), dto.attachmentIds());
        Message savedMessage = messageRepository.save(newMessage);

        // ChannelLastMessage 시간 추가
        Channel channel = channelRepository.findById(dto.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
        channel.updateRecentMessageTime(savedMessage.getCreateAt());

        return MessageResponseDTO.from(savedMessage);

        // 여기서 내가 생각했던 방식이랑 깨진다.
        // - 내가 생각한 건 dto에 있는 걸 곧이 곧대로 믿는 것이 좋을까?
        // - 근데 또 생각해보니깐 그렇네, dto로 뽑은 걸 사용하는게 더 안 좋을 것 같기도?
        // - dto는 불변이 보장된다면 dto로 입력 받는 것이 더 좋은 것 같음
    }

    @Override
    public MessageResponseDTO updateMessage(
            UpdateMessageRequestDTO dto
    ) {
        // 검증 로직
        // - 메세지 존재 여부
        Message message = getMessage(dto.messageId());

        message.verifySender(dto.requestUserId()); // 근데 이걸 여기서 하면 서버 리소스 소모를 안한다는 장점이 있는건 알겠는데, DDD 측면?

        List<UUID> newAttachmentIds = dto.attachmentIds() == null ? new ArrayList<>() : new ArrayList<>(dto.attachmentIds());
        for (UUID fileId : newAttachmentIds) {
            binaryContentRepository.findById(fileId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_EXIST));
        }

        // 이전에 있던 첨부 파일 삭제
        for (UUID oldFileId : message.getAttachmentIds()) {
            if (!newAttachmentIds.contains(oldFileId)) {
                binaryContentRepository.deleteById(oldFileId);
            }
        }

        // 실행 로직
        message.updateContent(dto.content(), dto.requestUserId(), dto.attachmentIds());
        Message savedMessage = messageRepository.save(message);
        return MessageResponseDTO.from(savedMessage);
    }

    @Override
    public GetAllMessagesResponseDTO getMessagesByChannel(UUID requestUserId, UUID channelId) {
        // 검증 로직
        userJoinThisChannel(requestUserId, channelId);

        // 실행 로직
        List<MessageResponseDTO> list = messageRepository.findAllByChannelId(channelId).stream()
                .map(MessageResponseDTO::from)
                .toList();

        return GetAllMessagesResponseDTO.from(list);
    }

    @Override
    public void deleteMessage(
            DeleteMessageRequestDTO dto
    ) {
        // 검증 로직
        // - 메세지 존재 여부
        Message message = getMessage(dto.messageId());
        message.verifySender(dto.requestUserId());

        // 없다면 빈 리스트를 들고 있으므로 null 체크 X
        for (UUID fileId : message.getAttachmentIds()) {
            binaryContentRepository.deleteById(fileId);
        }

        // 실행 로직
        messageRepository.deleteById(message.getId());
    }

    private UserChannel userJoinThisChannel(UUID userId, UUID channelId) {
        UserChannel uc = userChannelRepository.findByUserIdAndChannelId(userId, channelId)
                .orElseThrow(() -> new RuntimeException("가입된 채널에만 메세지를 보낼 수 있습니다."));
        return uc;
    }

    private Message getMessage(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("해당 메세지는 존재하지 않습니다."));
        return message;
    }
}
