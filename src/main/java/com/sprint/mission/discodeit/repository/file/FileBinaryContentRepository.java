package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FileBinaryContentRepository extends CommonFileRepository<BinaryContent> implements BinaryContentRepository {
    public FileBinaryContentRepository() {
        super("binarycontents", BinaryContent.class);
    }
}
