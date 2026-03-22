package com.sprint.mission.discodeit.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PrivateChannelCreateRequest {
    private UUID adminId;
    private List<UUID> memberId;// 비공개 채널 초기멤버 리스트
}