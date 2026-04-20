@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;

  @Transactional
  public MessageDto create(MessageCreateRequest request) {
    // 1. 작성자와 채널 엔티티 조회
    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new NoSuchElementException("채널을 찾을 수 없습니다."));

    // 2. 메시지 엔티티 생성 및 연관 관계 설정
    Message message = Message.builder()
        .content(request.content())
        .author(author)
        .channel(channel)
        .build();

    // 3. 저장 및 DTO 반환
    return MessageMapper.toDto(messageRepository.save(message));
  }
}