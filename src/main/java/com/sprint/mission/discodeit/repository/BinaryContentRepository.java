package com.sprint.mission.discodeit.repository;

// WHY ?? 왜 인터페이스만 만드는가???
// 현재 JCF, File 둘 다 사용중이기 때문에 저장과 비즈니스 로직 분리하기 위함

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

public interface BinaryContentRepository {

    BinaryContent save(BinaryContent binaryContent);

    BinaryContent findById(UUID id);

    List<BinaryContent> findAll();

    void delete(UUID id);
}
