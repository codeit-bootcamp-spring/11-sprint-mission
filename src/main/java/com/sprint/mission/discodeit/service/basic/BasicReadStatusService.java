package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequestDto;
import com.sprint.mission.discodeit.dto.ReadStatusResponseDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequestDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {
    private final ReadStatusRepository readStatusRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Override
    public ReadStatusResponseDto create(ReadStatusCreateRequestDto dto) {
        // 채널과 유저 존재 체크
        channelRepository.findById(dto.getChannelId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));
        userRepository.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 동일 조합 중복생성 방지
        boolean exists = readStatusRepository.findAllByUserId(dto.getUserId()).stream()
                .anyMatch(rs -> rs.getChannelId().equals(dto.getChannelId()));
        if (exists) {
            throw new IllegalArgumentException("이미 등록된 계정이 있습니다.");
        }
        ReadStatus readStatus = new ReadStatus(dto.getUserId(), dto.getChannelId(), dto.getLastReadAt());
        readStatusRepository.save(readStatus);
        return toResponseDto(readStatus);
    }

    @Override
    public ReadStatusResponseDto findById(UUID id) {
        ReadStatus entity = readStatusRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 ID 계정없음"));
        return toResponseDto(entity);
    }

    @Override
    public List<ReadStatusResponseDto> findAllByUserId(UUID userId) {
        List<ReadStatus> list = readStatusRepository.findAllByUserId(userId);
        return list.stream().map(this::toResponseDto).toList();
    }

    @Override
    public ReadStatusResponseDto update(ReadStatusUpdateRequestDto dto) {
        ReadStatus entity = readStatusRepository.findById(dto.getId()).orElseThrow(() -> new IllegalArgumentException("해당 ID 의 계정 정보 없음"));
        entity.setLastReadAt(dto.getLastReadAt());
        readStatusRepository.save(entity);
        return toResponseDto(entity);
    }

    @Override
    public void delete(UUID id) {
        readStatusRepository.deleteById(id);

    }

    private ReadStatusResponseDto toResponseDto(ReadStatus e) {
        ReadStatusResponseDto dto = new ReadStatusResponseDto();
        dto.setId(e.getId());
        dto.setUserId(e.getUserId());
        dto.setChannelId(e.getChannelId());
        dto.setLastReadAt(e.getLastReadAt());
        return dto;
    }

    @Override
    public ReadStatusResponseDto updateReadStatus(
            UUID channelId,
            UUID messageId,
            UUID readStatusId,
            ReadStatusUpdateRequestDto dto
    ) {
        ReadStatus entity = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new IllegalArgumentException("수신 정보를 찾을 수 없습니다."));
        // channelId, messageId는 추가로 체크할 수도 있음(로직에 따라)
        entity.setLastReadAt(dto.getLastReadAt());
        readStatusRepository.save(entity);
        return toResponseDto(entity);
    }

    @Override
    public List<ReadStatusResponseDto> findReadStatusByUserId(UUID userId) {
        // 1. repository에서 정보 조회
        List<ReadStatus> readStatusList = readStatusRepository.findAllByUserId(userId);
        // 2. entity를 DTO로 변환
        return readStatusList.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReadStatusResponseDto createReadStatus(UUID channelId, UUID messageId, ReadStatusCreateRequestDto dto) {
        // 1. 새로운 ReadStatus 엔티티 생성
        ReadStatus entity = new ReadStatus(
                dto.getUserId(),
                channelId,
                dto.getLastReadAt(), // 또는 생성 시각 등
                dto.isRead()
        );
        // 2. 저장
        readStatusRepository.save(entity);

        // 3. 응답 DTO로 변환해서 반환
        return new ReadStatusResponseDto(entity);
    }

}
