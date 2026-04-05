package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.CreateBinaryContentRequestDTO;
import com.sprint.mission.discodeit.dto.message.*;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserChannelRepository userChannelRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentService binaryContentService;

    @Override
    public MessageDto createMessage(MessageCreateRequest request, List<MultipartFile> attachments) {
        // 1. 해당 유저가 채널에 가입되어 있는지 확인 (권한 체크)
        userChannelRepository.findByUserIdAndChannelId(request.authorId(), request.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_JOINED_CHANNEL));

        // 2. 첨부 파일 저장 로직
        List<UUID> attachmentIds = new ArrayList<>();
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                if (file.isEmpty()) continue;
                try {
                    CreateBinaryContentRequestDTO fileDto = new CreateBinaryContentRequestDTO(
                            file.getOriginalFilename(),
                            file.getContentType(),
                            file.getBytes()
                    );
                    attachmentIds.add(binaryContentService.create(fileDto).getId());
                } catch (IOException e) {
                    throw new BusinessException(ErrorCode.FILE_IO_ERROR);
                }
            }
        }

        // 3. 메세지 생성 및 저장
        Message newMessage = Message.create(request.content(), request.channelId(), request.authorId(), attachmentIds);
        Message savedMessage = messageRepository.save(newMessage);

        // 4. 채널의 마지막 메세지 시간 업데이트
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
        channel.updateRecentMessageTime(savedMessage.getCreateAt());
        channelRepository.save(channel);

        return MessageDto.from(savedMessage);
    }

    @Override
    public MessageDto updateMessage(UUID requestUserId, UUID messageId, MessageUpdateRequest request) {
        Message message = getMessage(messageId);

        // 작성자 본인만 수정 가능
        message.verifySender(requestUserId);

        // API 명세서에 따르면 메세지 '내용(content)'만 수정 가능 (첨부파일 수정 X)
        message.updateContent(request.newContent(), requestUserId, message.getAttachmentIds());

        return MessageDto.from(messageRepository.save(message));
    }

    @Override
    public void deleteMessage(UUID requestUserId, UUID messageId) {
        Message message = getMessage(messageId);

        // 작성자 본인만 삭제 가능
        message.verifySender(requestUserId);

        // 1. 첨부 파일 연관 삭제
        if (message.getAttachmentIds() != null) {
            message.getAttachmentIds().forEach(binaryContentService::delete);
        }

        // 2. 메세지 삭제
        messageRepository.deleteById(messageId);
    }

    @Override
    public List<MessageDto> findAllByChannelId(UUID requestUserId, UUID channelId) {
        // 채널에 속해있는 사람만 메세지 조회 가능
        userChannelRepository.findByUserIdAndChannelId(requestUserId, channelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_JOINED_CHANNEL));

        return messageRepository.findAllByChannelId(channelId).stream()
                .map(MessageDto::from)
                .toList();
    }

    // 유틸 메서드
    private Message getMessage(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));
    }
}