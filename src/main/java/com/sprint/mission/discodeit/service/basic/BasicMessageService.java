package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequestDto;
import com.sprint.mission.discodeit.dto.MessageCreateRequestDto;
import com.sprint.mission.discodeit.dto.MessageResponseDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequestDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public MessageResponseDto create(UUID channelId, MessageCreateRequestDto dto) {
        // 메시지 저장
        Message message = new Message(dto.getContent(), dto.getAuthorId(), channelId);
        messageRepository.save(message);
        List<UUID> attachmentIds = new ArrayList<>();
        if (dto.getAttachments() != null) {
            for (BinaryContentCreateRequestDto contentDto : dto.getAttachments()) {
                BinaryContent file = new BinaryContent("text.png", contentDto.getContent(), contentDto.getContentType(), null, message.getId());
            }
        }
        return toResponseDto(message, attachmentIds);
    }

    @Override
    public List<MessageResponseDto> findAllByChannelId(UUID channelId) {
        List<Message> messages = messageRepository.findByChannelId(channelId);
        List<MessageResponseDto> result = new ArrayList<>();
        for (Message msg : messages) {
            List<UUID> attachmentIds = binaryContentRepository
                    .findAllByIdIn(List.of(msg.getId()))
                    // 메시지ID로 첨부파일 조회(실구현에 따라 조정)
                    .stream().map(BinaryContent::getId).toList();
            result.add(toResponseDto(msg, attachmentIds));
        }
        return result;
    }

    @Override
    public MessageResponseDto update(UUID channelId, UUID messageId, MessageUpdateRequestDto dto) {
        Message msg = messageRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("메시지 없음"));
        msg.updateContent(dto.getContent());
        messageRepository.save(msg);
        List<UUID> attachmentsIds = new ArrayList<>();
        if (dto.getAttachments() != null) {
            // 기존 파일 삭제(옵션), 새 파일 등록
            binaryContentRepository.findAllByIdIn(List.of(msg.getId()))
                    .forEach(f -> binaryContentRepository.deleteById(f.getId()));
            for (BinaryContentCreateRequestDto contentDto : dto.getAttachments()) {
                BinaryContent file = new BinaryContent("text.png", contentDto.getContent(), contentDto.getContentType(), null, msg.getId());
                binaryContentRepository.save(file);
                attachmentsIds.add(file.getId());
            }
        }
        return toResponseDto(msg, attachmentsIds);
    }

    @Override
    public void delete(UUID id, UUID messageId) {
        // 첨부파일 먼저 삭제
        binaryContentRepository.findAllByIdIn(List.of(id)).forEach(f -> binaryContentRepository.deleteById(f.getId()));
        messageRepository.deleteById(id);
    }

    private MessageResponseDto toResponseDto(Message msg, List<UUID> attachmentIds) {
        MessageResponseDto dto = new MessageResponseDto();
        dto.setId(msg.getId());
        dto.setChannelId(msg.getChannelId());
        dto.setAuthorId(msg.getAuthorId());
        dto.setContent(msg.getContent());
        dto.setCreatedAt(msg.getCreatedAt());
        dto.setUpdatedAt(msg.getUpdatedAt());
        dto.setAttachmentIds(attachmentIds);
        return dto;
    }
}

