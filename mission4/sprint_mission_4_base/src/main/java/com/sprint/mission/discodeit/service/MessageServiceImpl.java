@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public Message create(MessageCreateRequest request) {
        // 1. 메시지 엔티티 생성 및 저장
        Message message = new Message(request.getChannelId(), request.getSenderId(), request.getContent());
        messageRepository.save(message);

        // 2. 일대다 관계 처리: 첨부 파일이 있다면 메시지 ID를 부여하여 각각 저장
        if (request.getBinaryContentRequests() != null) {
            request.getBinaryContentRequests().forEach(req -> {
                // 도메인 제약 준수: 불변 객체 생성 (updatedAt 없음)
                BinaryContent binaryContent = new BinaryContent(
                        message.getId(),
                        req.getFileName(),
                        req.getSize(),
                        req.getContentType(),
                        req.getBytes()
                );
                binaryContentRepository.save(binaryContent);
            });
        }
        return message;
    }

    @Override
    public List<MessageDto> findAllByChannelId(UUID channelId) {
        // 3. 특정 채널의 모든 메시지 조회 (FileMessageRepository 활용)
        return messageRepository.findAllByChannelId(channelId).stream()
                .map(message -> {
                    // 4. 각 메시지에 첨부된 바이너리 콘텐츠들을 함께 조회
                    List<BinaryContent> contents = binaryContentRepository.findAllByMessageId(message.getId());
                    return convertToDto(message, contents);
                })
                .toList();
    }

    private MessageDto convertToDto(Message message, List<BinaryContent> contents) {
        return MessageDto.builder()
                .id(message.getId())
                .channelId(message.getChannelId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                // DTO 변환 시에도 불변 데이터 전달
                .binaryContents(contents.stream().map(c -> new BinaryContentDto(c.getId(), c.getFileName())).toList())
                .build();
    }
}