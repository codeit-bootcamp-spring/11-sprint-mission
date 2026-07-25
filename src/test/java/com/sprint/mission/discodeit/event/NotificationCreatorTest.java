package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.authority.UserRole;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NotificationCreatorTest {

    @Mock
    NotificationRepository notificationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    NotificationMapper notificationMapper;

    @Mock
    CacheManager cacheManager;

    @Mock
    Cache cache;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    NotificationCreator notificationCreator;

    @Test
    void createMessageNotifications_savesAllAndEvictsCaches() {
        // given
        User firstReceiver = new User("evan", "evan@test.com", "password");
        User secondReceiver = new User("wendy", "wendy@test.com", "password");
        UserDto author = new UserDto(
                UUID.randomUUID(),
                "author",
                "author@test.com",
                null,
                true,
                UserRole.USER
        );
        MessageDto message = new MessageDto(
                UUID.randomUUID(),
                null,
                null,
                "hello",
                UUID.randomUUID(),
                author,
                List.of()
        );
        MessageCreatedEvent event = new MessageCreatedEvent(
                message,
                "general",
                List.of(firstReceiver.getId(), secondReceiver.getId())
        );

        given(userRepository.findAllById(event.receiverIds()))
                .willReturn(List.of(firstReceiver, secondReceiver));
        given(notificationRepository.saveAll(org.mockito.ArgumentMatchers.anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(cacheManager.getCache("notificationsByReceiver")).willReturn(cache);

        // when
        notificationCreator.create(event);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Notification>> notificationsCaptor =
                ArgumentCaptor.forClass(List.class);
        then(notificationRepository).should().saveAll(notificationsCaptor.capture());
        assertThat(notificationsCaptor.getValue())
                .hasSize(2)
                .allSatisfy(notification -> {
                    assertThat(notification.getTitle()).isEqualTo("author (#general)");
                    assertThat(notification.getContent()).isEqualTo("hello");
                });
        then(cache).should().evict(firstReceiver.getId());
        then(cache).should().evict(secondReceiver.getId());
    }
}
