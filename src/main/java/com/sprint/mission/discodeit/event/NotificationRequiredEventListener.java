package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredEventListener {

    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener
    public void on(MessageCreatedEvent event) {
        log.debug("메시지 생성 알림 처리 시작: messageId={}", event.message().id());

        Channel channel = channelRepository.findById(event.message().channelId())
                .orElseThrow(() ->
                        ChannelNotFoundException.withId(event.message().channelId()));

        List<ReadStatus> readStatuses = readStatusRepository.findAllByChannelIdWithUser(event.message().channelId());

        readStatuses.stream()
                .filter(ReadStatus::isNotificationEnabled)
                .filter(readStatus -> !readStatus.getUser().getId().equals(event.message().author().id()))
                .forEach(readStatus -> notificationService.create("%s (#%s)".formatted(event.message().author().username(),
                        channel.getName()), event.message().content(), readStatus.getUser().getId()));

        log.info("메시지 생성 알림 처리 완료: messageId={}", event.message().id());
    }

    @Async
    @TransactionalEventListener
    public void on(RoleUpdatedEvent event) {
        log.debug("권한 변경 알림 처리 시작: userId={}", event.userId());

        notificationService.create("권한이 변경되었습니다.", "%s -> %s".formatted(event.oldRole(), event.newRole()),
                event.userId()
        );
        log.info("권한 변경 알림 처리 완료: userId={}", event.userId());
    }

}
