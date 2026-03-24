package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FILEUserStatusRepository implements UserStatusRepository {

    //UserStatus는 저장시 UserId를 키로 하므로 saveLoad를 안쓰고 새로 만듬
    private final Path directory;


    public FILEUserStatusRepository(@Value("${discodeit.repository.file-dir}") String path) {
        this.directory = Path.of(  path + "/UserStatuses/");
    }

    @Override
    public boolean saveUserStatus(UserStatus userStatus) {

        if(userStatus == null)
            return false;

        save(idToPath(userStatus.getUserId()),userStatus);
        return true;
    }

    @Override
    public Optional<UserStatus> getUserStatus(UUID userId) {
        Map<UUID,UserStatus> userStatuses = load(directory);
        return Optional.ofNullable(userStatuses.get(userId));

    }

    @Override
    public List<UserStatus> getAllUserStatus() {
        Map<UUID,UserStatus> userStatuses = load(directory);
        return userStatuses.values().stream().toList();
    }

    @Override
    public boolean deleteUserStatus(UUID userId) {
        if(!isExistUserStatus(userId)){
            return false;
        }

        try {
            Files.deleteIfExists(idToPath(userId));
        }
        catch(IOException e){
            return false;
        }
        return true;
    }

    @Override
    public boolean isExistUserStatus(UUID userId) {
        Map<UUID,UserStatus> userStatuses = load(directory);
        return userStatuses.containsKey(userId);
    }

    private Map<UUID,UserStatus> load(Path directory){

        if (Files.exists(directory)) {


            try (Stream<Path> stream =  Files.list(directory))

            {
                Map<UUID, UserStatus> map;


                map = stream.map(path -> {
                            try (
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis)
                            ) {
                                Object data = ois.readObject();
                                return  (UserStatus)data;
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toMap(
                                UserStatus::getUserId,
                                Function.identity()

                        ));
                return map;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new HashMap<>();
        }
    }

    private void save(Path filePath, UserStatus userStatus){

        try{
            Files.createDirectories(filePath.getParent());
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }

        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(userStatus);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }


    private Path idToPath(UUID userStatusId){

        return directory.resolve(userStatusId+ ".dat");

    }


}
