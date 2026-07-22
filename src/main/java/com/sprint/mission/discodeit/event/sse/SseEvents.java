package com.sprint.mission.discodeit.event.sse;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import java.util.Collection;
import java.util.UUID;

public class SseEvents {

  public record NotificationCreatedEvent(NotificationDto data) {

  }

  public record ChannelCreatedEvent(ChannelDto data, Collection<UUID> participantIds) {

  }

  public record ChannelUpdatedEvent(ChannelDto data) {

  }

  public record ChannelDeletedEvent(ChannelDto data) {

  }

  public record UserCreatedEvent(UserDto data) {

  }

  public record UserUpdatedEvent(UserDto data) {

  }

  public record UserDeletedEvent(UserDto data) {

  }

  public record BinaryContentUpdatedEvent(UUID fileId, BinaryContentStatus status) {

  }
}