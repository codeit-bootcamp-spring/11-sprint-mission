public class ChannelMapper {

  public static ChannelDto toDto(Channel channel) {
    return new ChannelDto(
        channel.getId(),
        channel.getType(),
        channel.getName(),
        channel.getDescription(),
        // participants를 UserDto 리스트로 변환하는 로직 (나중에 구현)
        java.util.Collections.emptyList(),
        null
    );
  }
}