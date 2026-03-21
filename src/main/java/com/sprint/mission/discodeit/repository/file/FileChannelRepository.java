package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.base.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class FileChannelRepository extends FileRepository<Channel> implements ChannelRepository {

    protected FileChannelRepository(@Value("${app.data.channel-path}") String filePath) {
        super(filePath);
    }

    @Override
    protected void postLoad() {

    }
}
