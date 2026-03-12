package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Domain.BinaryContent;

import java.util.List;
import java.util.UUID;

public interface BinaryContentRepository {

    // 파일, 이미지를 생성
    BinaryContent create(BinaryContent binaryContent);

    // user 프로필 조회 > userid 기반
    BinaryContent readByUserId(UUID userId);

    // message 첨부파일 조회 > messageid 기반
    List<BinaryContent> readAllByMessageId(UUID messageId);

    // 이건 다른거랑 다르게 자체의 uuid로 삭제
    void delete(UUID id);
}
