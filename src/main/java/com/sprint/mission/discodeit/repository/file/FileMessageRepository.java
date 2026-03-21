package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class FileMessageRepository extends FileRepository<Message> implements MessageRepository {
    public FileMessageRepository() {
        super(Message.class);
    }

    @Override
    public void save(Message message) {
        super.save(message);
    }

    @Override
    public Message findById(UUID id) {
        Message message = super.findById(id);
        if (message == null) throw new IllegalArgumentException("requested message not found. ❌");

        return message;
    }

    @Override
    public List<Message> findAll() {
        return super.findAll();
    }

    @Override
    public void delete(Message message) {
        super.delete(message);
    }
}
