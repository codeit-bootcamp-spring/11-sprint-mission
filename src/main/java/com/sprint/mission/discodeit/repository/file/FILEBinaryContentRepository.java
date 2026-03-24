package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FILEBinaryContentRepository implements BinaryContentRepository {

    private final FileSaveLoad<BinaryContent> saveLoad;
    private final Path directory;

    public FILEBinaryContentRepository(@Value("${discodeit.repository.file-dir}") String path) {
        this.saveLoad = new FileSaveLoad<>();
        this.directory = Path.of( path + "/BinaryContents/");
    }

    @Override
    public BinaryContent saveBinaryContent(BinaryContent binaryContent) {

        if(binaryContent== null)
            return null;
        saveLoad.save(idToPath(binaryContent.getId()),binaryContent);
        return binaryContent;
    }

    @Override
    public Optional<BinaryContent> getBinaryContent(UUID binaryContentId) {
        Map<UUID,BinaryContent> binaryContents = saveLoad.load(directory);
        return Optional.ofNullable(binaryContents.get(binaryContentId));
    }

    @Override
    public Optional<BinaryContent> getProfileContentByUserId(UUID userId) {
        Map<UUID,BinaryContent> binaryContents = saveLoad.load(directory);
        return binaryContents.values().stream()
                .filter(binaryContent -> binaryContent.getUserID().equals(userId))
                .findFirst();
    }

    @Override
    public List<BinaryContent> getAllBinaryContent() {
        Map<UUID,BinaryContent> binaryContents = saveLoad.load(directory);
        return binaryContents.values().stream().toList();
    }

    @Override
    public List<BinaryContent> getAllByUserId(UUID userId) {
        Map<UUID,BinaryContent> binaryContents = saveLoad.load(directory);
        return binaryContents.values().stream()
                .filter(binaryContent -> binaryContent.getUserID().equals(userId))
                .toList();
    }

    @Override
    public List<BinaryContent> getAllByMessageId(UUID messageId) {
        Map<UUID,BinaryContent> binaryContents = saveLoad.load(directory);
        return binaryContents.values().stream()
                .filter(binaryContent -> binaryContent.getMessageId()!=null&&binaryContent.getMessageId().equals(messageId))
                .toList();
    }

    @Override
    public boolean deleteBinaryContent(UUID binaryContentId) {
        if(!isExistBinaryContent(binaryContentId)){
            return false;
        }
        try{
            Files.deleteIfExists(idToPath(binaryContentId));

        }
        catch(Exception e){
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteBinaryContentByMessageId(UUID messageId) {
        Map<UUID,BinaryContent> binaryContents = saveLoad.load(directory);
        binaryContents.values().stream().filter(binaryContent -> binaryContent.getMessageId()!=null&&binaryContent.getMessageId().equals(messageId))
                .forEach(binaryContent -> deleteBinaryContent(binaryContent.getId()));
        return true;
    }

    @Override
    public boolean isExistBinaryContent(UUID binaryContentId) {
       Map<UUID,BinaryContent> binaryContents = saveLoad.load(directory);
       return binaryContents.containsKey(binaryContentId);
    }



    private Path idToPath(UUID binaryContentId){
        return directory.resolve(binaryContentId +".dat");
    }
}
