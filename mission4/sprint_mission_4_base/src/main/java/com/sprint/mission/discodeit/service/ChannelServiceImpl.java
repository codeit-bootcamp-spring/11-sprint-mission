@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;

    // 1. Public 채널 생성
    @Override
    public Channel create(PublicChannelCreateRequest request) {
        Channel channel = new Channel(request.getName(), ChannelType.PUBLIC, null);
        return channelRepository.save(channel);
    }

    // 2. Private 채널 생성 (ReadStatus 동시 생성 로직 포함)
    @Override
    public Channel create(PrivateChannelCreateRequest request) {
        Channel channel = new Channel(request.getName(), ChannelType.PRIVATE, request.getMemberIds());
        channelRepository.save(channel);

        // 요구사항: Private 채널 생성 시 참여 유저별 ReadStatus 생성
        if (request.getMemberIds() != null) {
            request.getMemberIds().forEach(memberId -> {
                ReadStatus readStatus = new ReadStatus(memberId, channel.getId());
                readStatusRepository.save(readStatus);
            });
        }
        return channel;
    }

    // 3. 채널 목록 조회 (권한 필터링 로직)
    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        // 모든 채널을 가져와서 필터링 수행
        return channelRepository.findAll().stream()
                .filter(channel -> {
                    // Public 채널은 모든 유저에게 노출
                    if (channel.getType() == ChannelType.PUBLIC) {
                        return true;
                    }
                    // Private 채널은 참여자인 경우에만 노출
                    return channel.getMemberIds() != null && channel.getMemberIds().contains(userId);
                })
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public ChannelDto find(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("Channel not found"));
        return convertToDto(channel);
    }

    private ChannelDto convertToDto(Channel channel) {
        return ChannelDto.builder()
                .id(channel.getId())
                .name(channel.getName())
                .type(channel.getType())
                .build();
    }

    // update 및 delete 로직은 인터페이스 명세에 맞춰 구현...
}