package com.sprint.mission.discodeit.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContentUploadStatus;
import com.sprint.mission.discodeit.entity.Role;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class CacheConfigTest {

  private final SerializationPair<Object> serializationPair = redisCacheConfiguration()
      .getValueSerializationPair();

  private RedisCacheConfiguration redisCacheConfiguration() {
    return new CacheConfig().redisCacheConfiguration(Jackson2ObjectMapperBuilder.json().build());
  }

  @Test
  @DisplayName("사용자 목록 캐시 값이 직렬화 후 동일하게 복원된다")
  void userDtoList_RoundTrip() {
    List<UserDto> users = List.of(new UserDto(
        UUID.randomUUID(),
        "tester",
        "tester@example.com",
        new BinaryContentDto(UUID.randomUUID(), "profile.jpg", 1024L, "image/jpeg",
            BinaryContentUploadStatus.SUCCESS),
        true,
        Role.USER
    ));

    Object restored = serializationPair.read(serializationPair.write(users));

    assertThat(restored).isEqualTo(users);
  }

  @Test
  @DisplayName("알림 목록 캐시 값의 Instant 필드가 손실 없이 복원된다")
  void notificationDtoList_RoundTrip() {
    List<NotificationDto> notifications = List.of(new NotificationDto(
        UUID.randomUUID(),
        Instant.now(),
        UUID.randomUUID(),
        "제목",
        "내용"
    ));

    Object restored = serializationPair.read(serializationPair.write(notifications));

    assertThat(restored).isEqualTo(notifications);
  }

  @Test
  @DisplayName("캐시 키에 discodeit 접두사와 TTL이 설정된다")
  void cacheConfiguration_HasPrefixAndTtl() {
    RedisCacheConfiguration configuration = redisCacheConfiguration();

    assertThat(configuration.getKeyPrefixFor("users")).isEqualTo("discodeit:users::");
    assertThat(configuration.getTtlFunction().getTimeToLive(Object.class, null))
        .hasSeconds(600);
    assertThat(configuration.getAllowCacheNullValues()).isFalse();
  }
}