package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.CreateReadStatusRequestDTO;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponseDTO;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
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
    public ReadStatusResponseDTO create(
            CreateReadStatusRequestDTO dto
    ) {
        // 검증
        // - Channel 검증
        Channel channel = channelRepository.findById(dto.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        // - User 검증
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean alreadyExists = readStatusRepository.findByUserId(user.getId()).stream()
                .anyMatch(rs -> rs.getChannelId().equals(channel.getId()));
        if (alreadyExists) {
            throw new BusinessException(ErrorCode.READ_STATUS_ALREADY_EXIST);
        }

        ReadStatus newReadStatus = ReadStatus.create(dto.userId(), dto.channelId());
        return ReadStatusResponseDTO.from(readStatusRepository.save(newReadStatus));
    }

    @Override
    public ReadStatusResponseDTO find(
            UUID readStatusId
    ) {
        return ReadStatusResponseDTO.from(readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new BusinessException(ErrorCode.READ_STATUS_NOT_FOUND)));
    }

    @Override
    public List<ReadStatusResponseDTO> findAllByUserId(
            UUID userId
    ) {
        // 검증
        // - 유저의 존재 여부
        userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return readStatusRepository.findByUserId(userId)
                .stream()
                .map(ReadStatusResponseDTO::from)
                .toList();
    }

    @Override
    public ReadStatusResponseDTO update(
            UUID readStatusId
    ) {
        ReadStatus readStatus = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new RuntimeException("해당 ReadStatus는 없습니다."));

        readStatus.updateReadAt();

        return ReadStatusResponseDTO.from(readStatusRepository.save(readStatus));
    }

    @Override
    public void delete(
            UUID readStatusId
    ) {
        // 이걸 삭제하는 경우는 뭐가 있을까?
        // 채널-유저 간의 관계가 사라졌을떼?
        ReadStatus readStatus = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new RuntimeException("해당 ReadStatus는 없습니다."));

        readStatusRepository.deleteById(readStatus.getId());
    }
}
