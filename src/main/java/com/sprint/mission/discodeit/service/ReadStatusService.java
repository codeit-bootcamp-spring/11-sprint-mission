package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequestDto;
import com.sprint.mission.discodeit.dto.ReadStatusResponseDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequestDto;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {
    // 메시지 읽음 상태 생성
    ReadStatusResponseDto create(ReadStatusCreateRequestDto dto);

    // 개별 조회
    ReadStatusResponseDto findById(UUID id);

    // 사용자별 전체 목록 조회
    List<ReadStatusResponseDto> findAllByUserId(UUID userId);

    // 특정 채널/메시지/수신정보 읽음 상태 갱신
    ReadStatusResponseDto updateReadStatus(UUID channelId, UUID messageId, UUID readStatusId, ReadStatusUpdateRequestDto dto);

    List<ReadStatusResponseDto> findReadStatusByUserId(UUID userId);

    // (Option) 메시지 읽음상태 id로 단순 수정
    ReadStatusResponseDto update(ReadStatusUpdateRequestDto dto);

    // 메시지 읽음상태 삭제
    void delete(UUID id);

    ReadStatusResponseDto createReadStatus(UUID channelId, UUID messageId, ReadStatusCreateRequestDto dto);
}
