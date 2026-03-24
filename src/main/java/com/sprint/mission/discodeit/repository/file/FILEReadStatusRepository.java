package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FILEReadStatusRepository implements ReadStatusRepository {
    private final FileSaveLoad<ReadStatus> saveLoad;
    private final Path directory;

    public FILEReadStatusRepository(@Value("${discodeit.repository.file-dir}") String path) {
        this.saveLoad = new FileSaveLoad<>();
        this.directory = Path.of( path + "/ReadStatuses/");
    }

    @Override
    public boolean save(ReadStatus readStatus) {
        if(readStatus == null)
            return false;
        save(idToPath(readStatus.getId()),readStatus);
        return true;

    }

    @Override
    public Optional<ReadStatus> get(UUID userId, UUID channelId) {
        Map<UUID,ReadStatus> readStatuses = load(directory);
        return readStatuses.values().stream().filter(readStatus -> readStatus.getUserId().equals(userId) && readStatus.getChannelId().equals(channelId)).findFirst();

    }

    @Override
    public List<ReadStatus> getAll() {
        Map<UUID,ReadStatus> readStatuses = load(directory);
        return readStatuses.values().stream().toList();
    }

    @Override
    public List<ReadStatus> getAllByUserId(UUID userId) {
        Map<UUID,ReadStatus> readStatuses = load(directory);
        return readStatuses.values().stream().filter(readStatus -> readStatus.getUserId().equals(userId)).toList();
    }

    @Override
    public List<ReadStatus> getAllByChannelId(UUID channelId) {
        Map<UUID,ReadStatus> readStatuses = load(directory);
        return readStatuses.values().stream().filter(readStatus -> readStatus.getChannelId().equals(channelId)).toList();
    }

    @Override
    public boolean delete(UUID userId, UUID channelId) {
        if(!isExist(userId,channelId))
            return false;

        Map<UUID,ReadStatus> readStatuses = load(directory);
        UUID readStatusId = readStatuses.values().stream().filter(readStatus -> readStatus.getUserId().equals(userId) && readStatus.getChannelId().equals(channelId)).findFirst().orElseThrow().getId();

        try {
            Files.deleteIfExists(idToPath(readStatusId));
        }
        catch(IOException e){
            return false;
        }
        return true;
    }

    @Override
    public boolean isExist(UUID userId, UUID channelId) {
        Map<UUID,ReadStatus> readStatuses = load(directory);
        return readStatuses.values().stream().anyMatch(readStatus -> readStatus.getUserId().equals(userId) && readStatus.getChannelId().equals(channelId));
    }

    private Map<UUID,ReadStatus> load(Path directory) {

        return saveLoad.load(directory);
    }

    private void save(Path filePath, ReadStatus readStatus) {
        saveLoad.save(filePath, readStatus);
    }



    private Path idToPath(UUID readStatusId) {
        return directory.resolve(readStatusId +".dat");
    }
}
