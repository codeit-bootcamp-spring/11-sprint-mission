package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "file"
)
public class FileMessageRepository extends CommonFileRepository<Message> implements MessageRepository {
    public FileMessageRepository(
            @Value("${discodeit.repository.file.base-dir}") String basedir
    ) {
        super("messages", Message.class, basedir);
    }
}
