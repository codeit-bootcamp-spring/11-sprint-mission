package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements com.sprint.mission.discodeit.service.ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ReadStatus create(ReadStatusCreateRequest dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));
        Channel channel = channelRepository.findById(dto.channelId())
                .orElseThrow(() -> new IllegalArgumentException("없는 channel id 입니다."));

        if (readStatusRepository.existsByUserIdAndChannelId(user.getId(), channel.getId())) {
            throw new IllegalArgumentException("이미 존재하는 ReadStatus 입니다.");
        }

        ReadStatus readStatus = new ReadStatus(user.getId(), channel.getId(), Instant.now());

        return readStatusRepository.save(readStatus);
    }

    @Override
    public ReadStatus find(UUID id) {
        return readStatusRepository.find(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 readStatus id 입니다."));
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));
        return readStatusRepository.findAllByUserId(user.getId());
    }

    // 채팅방 읽음
    @Override
    public ReadStatus update(ReadStatusUpdateRequest dto) {
        ReadStatus readStatus = readStatusRepository.findAllByUserId(dto.userId()).stream()
                .filter(rs -> rs.getChannelId().equals(dto.channelId()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("해당 유저와 채널의 readStatus가 생성되지 않았습니다."));

        readStatus.updateLastReadAt();

        return readStatusRepository.save(readStatus);
    }

    @Override
    public void delete(UUID id) {
        ReadStatus readStatus = readStatusRepository.find(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 readStatus id 입니다."));

        readStatusRepository.delete(readStatus.getId());
    }
}
