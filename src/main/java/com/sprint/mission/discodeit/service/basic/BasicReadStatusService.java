package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.CreateReadStatusRequestDTO;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    public ReadStatus create(
            CreateReadStatusRequestDTO dto
    ) {
        // 검증
        // - Channel 검증
        Channel channel = channelRepository.findById(dto.channelId())
                .orElseThrow(() -> new RuntimeException("해당 채널이 없습니다."));

        // - User 검증
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("해당 유저가 없습니다."));

        boolean alreadyExists = readStatusRepository.findByUserId(user.getId()).stream()
                .anyMatch(rs -> rs.getChannelId().equals(channel.getId()));
        if (alreadyExists) {
            throw new RuntimeException("해당 유저는 이미 이 채널의 읽기 상태를 가지고 있습니다.");
        }

        ReadStatus newReadStatus = ReadStatus.create(dto.userId(), dto.channelId());
        return readStatusRepository.save(newReadStatus);
    }

    @Override
    public ReadStatus find(
            UUID readStatusId
    ) {
        return readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new RuntimeException("해당 ReadStatus는 없습니다."));
    }

    @Override
    public List<ReadStatus> findAllByUserId(
            UUID userId
    ) {
        // 검증
        // - 유저의 존재 여부
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("해당 유저는 존재하지 않습니다."));

        return readStatusRepository.findByUserId(userId);
    }

    @Override
    public ReadStatus update(
            UUID readStatusId
    ) {
        ReadStatus readStatus = find(readStatusId);

        readStatus.updateReadAt();

        return readStatusRepository.save(readStatus);
    }

    @Override
    public void delete(
            UUID readStatusId
    ) {
        // 이걸 삭제하는 경우는 뭐가 있을까?
        // 채널-유저 간의 관계가 사라졌을떼?
        ReadStatus readStatus = find(readStatusId);

        readStatusRepository.deleteById(readStatus.getId());
    }
}
