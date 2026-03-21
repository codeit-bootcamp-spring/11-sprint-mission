package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "file"
)
public class FileChannelRepository extends CommonFileRepository<Channel> implements ChannelRepository {
    public FileChannelRepository(
            @Value("${discodeit.repository.file.base-dir}") String basedir
    ) {
        super("channels", Channel.class, basedir);
    }
}
