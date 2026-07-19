package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicNotificationService implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
        log.debug("사용자별 알림 목록 조회 시작: receiverId={}", receiverId);
        List<NotificationDto> dtos =
                notificationRepository.findAllByReceiverId(receiverId).stream()
                        .map(notificationMapper::toDto)
                        .toList();
        log.info("사용자별 알림 목록 조회 완료: receiverId={}, 조회된 항목 수={}", receiverId, dtos.size());
        return dtos;
    }

    @Transactional(readOnly = true)
    @Override
    public NotificationDto find(UUID notificationId) {
        log.debug("알림 조회 시작: id={}", notificationId);
        NotificationDto dto = notificationRepository.findById(notificationId)
                .map(notificationMapper::toDto)
                .orElseThrow(() -> NotificationNotFoundException.withId(notificationId));
        log.info("알림 조회 완료: id={}", notificationId);
        return dto;
    }

    @Transactional
    @Override
    public NotificationDto create(String title, String content, UUID receiverId) {
        log.debug("알림 생성 시작: receiverId={}", receiverId);
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> UserNotFoundException.withId(receiverId));
        Notification notification = notificationRepository.save(new Notification(title, content, receiver));
        log.info("알림 생성 완료: id={}, receiverId={}", notification.getId(), receiverId);
        return notificationMapper.toDto(notification);
    }

    @PreAuthorize("principal.userDto.id == @basicNotificationService.find(#notificationId).receiverId")
    @Transactional
    @Override
    public void delete(UUID notificationId) {
        log.debug("알림 삭제 시작: id={}", notificationId);
        if (!notificationRepository.existsById(notificationId)) {
            throw NotificationNotFoundException.withId(notificationId);
        }
        notificationRepository.deleteById(notificationId);
        log.info("알림 삭제 완료: id={}", notificationId);
    }
}
